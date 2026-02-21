import os
import cv2
import csv
import numpy as np
import sys

from video_processor import extract_frames, detect_timestamp_anomalies
from motion_detector import compute_motion, detect_merge
from visualizer import annotate_frame

# -------- INPUT FROM FLASK --------
input_video = None
output_video_override = None

if len(sys.argv) > 1:
    input_video = sys.argv[1]

if len(sys.argv) > 2:
    output_video_override = sys.argv[2]

# -------- OUTPUT FOLDER --------
os.makedirs("outputs", exist_ok=True)

# -------- INPUT HANDLING --------
if input_video:
    video_files = [input_video]
else:
    video_files = [
        os.path.join("inputs", f)
        for f in os.listdir("inputs")
        if f.endswith(".mp4")
    ]

# -------- PROCESS --------
for video_path in video_files:

    video_file = os.path.basename(video_path)
    print(f"\nProcessing: {video_path}")

    # -------- OUTPUT PATH --------
    if output_video_override:
        output_video_path = output_video_override
    else:
        output_video_path = os.path.join("outputs", video_file + "_output.mp4")

    # -------- TEMP FILE (IMPORTANT) --------
    temp_path = os.path.join("outputs", "temp.mp4")

    # -------- CSV PATH --------
    csv_filename = os.path.splitext(os.path.basename(output_video_path))[0] + ".csv"
    csv_path = os.path.join("outputs", csv_filename)

    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        print("❌ Error opening video")
        continue

    # 🔥 FIX FPS
    fps = cap.get(cv2.CAP_PROP_FPS)
    if fps == 0 or fps > 60:
        fps = 30

    print("Using FPS:", fps)

    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

    # 🔥 FIX HEIGHT
    if height % 2 != 0:
        height -= 1

    cap.release()

    # -------- EXTRACT FRAMES --------
    frames, timestamps = extract_frames(video_path)

    # -------- FALLBACK --------
    if len(frames) < 2:
        print("⚠ Not enough frames — creating fallback video")

        fourcc = cv2.VideoWriter_fourcc(*'mp4v')
        output = cv2.VideoWriter(temp_path, fourcc, fps, (width, height))

        for _ in range(10):
            blank = np.zeros((height, width, 3), dtype=np.uint8)
            output.write(blank)

        output.release()

        # 🔥 FFmpeg conversion
        os.system(f'ffmpeg -y -i "{temp_path}" -vcodec libx264 -pix_fmt yuv420p "{output_video_path}"')

        if os.path.exists(temp_path):
            os.remove(temp_path)

        print("✅ Fallback video created:", output_video_path)
        continue

    # -------- FIRST PASS --------
    all_scores = []
    all_motion = []

    for i in range(1, len(frames)):
        motion = compute_motion(frames[i-1], frames[i])
        _, score = detect_merge(frames[i-1], frames[i])

        all_scores.append(score)
        all_motion.append(motion)

    mean_score = np.mean(all_scores)
    std_score = np.std(all_scores)

    merge_threshold = mean_score - 1.5 * std_score
    max_motion = max(all_motion) + 1e-6

    timestamp_flags = detect_timestamp_anomalies(timestamps, fps)

    # -------- VIDEO WRITER (TEMP) --------
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')

    output = cv2.VideoWriter(
        temp_path,
        fourcc,
        fps,
        (width, height)
    )

    if not output.isOpened():
        print("❌ VideoWriter failed!")
        continue

    drop_count = 0
    merge_count = 0

    # -------- CSV --------
    with open(csv_path, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["Frame", "Label"])

        for i in range(1, len(frames)):

            motion = compute_motion(frames[i-1], frames[i])
            merge_flag, score = detect_merge(frames[i-1], frames[i])

            motion_norm = motion / max_motion

            if timestamp_flags[i] == "DROP":
                label = "Frame Drop"
            elif score < merge_threshold:
                label = "Frame Merge"
            elif motion_norm < 0.4:
                label = "Frame Merge"
            else:
                label = "Normal"

            if label == "Frame Drop":
                drop_count += 1
            elif label == "Frame Merge":
                merge_count += 1

            frame = annotate_frame(frames[i], label)
            frame = cv2.resize(frame, (width, height))

            output.write(frame)
            writer.writerow([i, label])

    output.release()

    # -------- 🔥 FINAL CONVERSION --------
    os.system(
        f'ffmpeg -y -i "{temp_path}" -vcodec libx264 -pix_fmt yuv420p "{output_video_path}"'
    )

    if os.path.exists(temp_path):
        os.remove(temp_path)

    # -------- SUMMARY --------
    total_frames = len(frames)

    print(f"\n--- SUMMARY for {video_file} ---")
    print(f"Total Frames: {total_frames}")
    print(f"Frame Drops: {drop_count}")
    print(f"Frame Merges: {merge_count}")
    print(f"Normal Frames: {total_frames - drop_count - merge_count}")

    print(f"✅ Saved video: {output_video_path}")
    print(f"📄 CSV saved: {csv_path}")