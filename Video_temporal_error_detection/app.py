import gradio as gr
import subprocess
import uuid
import os
import shutil

UPLOAD_DIR = "uploads"
OUTPUT_DIR = "outputs"

os.makedirs(UPLOAD_DIR, exist_ok=True)
os.makedirs(OUTPUT_DIR, exist_ok=True)


def process_video(video):

    if video is None:
        return None, None, "Please upload a video"

    # Gradio provides temp file path
    temp_input_path = video.name

    filename = f"{uuid.uuid4().hex}.mp4"

    input_path = os.path.join(UPLOAD_DIR, filename)

    output_video_name = "processed_" + filename
    output_video_path = os.path.join(OUTPUT_DIR, output_video_name)

    # CSV name must match main.py logic
    csv_name = os.path.splitext(output_video_name)[0] + ".csv"
    csv_path = os.path.join(OUTPUT_DIR, csv_name)

    try:
        # Copy uploaded file
        shutil.copy(temp_input_path, input_path)

        # Run main.py
        result = subprocess.run(
            ["python", "main.py", input_path, output_video_path],
            capture_output=True,
            text=True,
            timeout=600   # 10 min
        )

        # Check error
        if result.returncode != 0:
            print("Main.py error:", result.stderr)
            return None, None, "Processing failed ❌"

        # Check output files
        if not os.path.exists(output_video_path):
            return None, None, "Video not generated ❌"

        if not os.path.exists(csv_path):
            return output_video_path, None, "CSV not generated ⚠️"

        return output_video_path, csv_path, "Completed ✅"

    except subprocess.TimeoutExpired:
        return None, None, "Processing timeout ⏱️"

    except Exception as e:
        return None, None, f"Error: {str(e)}"


interface = gr.Interface(
    fn=process_video,

    inputs=gr.File(label="Upload Video"),

    outputs=[
        gr.Video(label="Processed Video"),
        gr.File(label="Download CSV Report"),
        gr.Textbox(label="Status")
    ],

    title="Video Temporal Error Detection",
    description="Upload a video → Get processed video + CSV report"
)

interface.launch()