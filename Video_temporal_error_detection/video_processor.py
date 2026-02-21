import cv2

def extract_frames(video_path):
    cap = cv2.VideoCapture(video_path)
    frames = []
    timestamps = []

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        timestamp = cap.get(cv2.CAP_PROP_POS_MSEC)
        frames.append(frame)
        timestamps.append(timestamp)

    cap.release()
    return frames, timestamps

def detect_timestamp_anomalies(timestamps, fps):
    expected_gap = 1000 / fps  # ms
    flags = ["NORMAL"] * len(timestamps)

    for i in range(1, len(timestamps)):
        gap = timestamps[i] - timestamps[i-1]

        if gap > expected_gap * 1.5:
            flags[i] = "DROP"

    return flags