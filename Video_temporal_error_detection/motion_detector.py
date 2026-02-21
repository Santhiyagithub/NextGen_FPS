

import cv2
import numpy as np

# ✅ MOTION FUNCTION (MISSING — ADD THIS)
def compute_motion(prev_frame, curr_frame):
    prev_gray = cv2.cvtColor(prev_frame, cv2.COLOR_BGR2GRAY)
    curr_gray = cv2.cvtColor(curr_frame, cv2.COLOR_BGR2GRAY)

    flow = cv2.calcOpticalFlowFarneback(
        prev_gray, curr_gray, None,
        0.5, 3, 15, 3, 5, 1.2, 0
    )

    magnitude = np.sqrt(flow[...,0]**2 + flow[...,1]**2)
    return np.mean(magnitude)


# ✅ MERGE DETECTION (UPDATED)
def detect_merge(prev_frame, curr_frame):
    diff = cv2.absdiff(prev_frame, curr_frame)
    score = diff.mean()

    return score < 0.7, score