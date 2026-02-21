def annotate_frame(frame, label):
    import cv2

    if label == "Frame Drop":
        color = (0,0,255)
        text = "DROP ⚠"
    elif label == "Frame Merge":
        color = (255,0,0)
        text = "MERGE ⚠"
    else:
        color = (0,255,0)
        text = "NORMAL"
        

    # Bigger text
    cv2.putText(frame, text, (30,60),
                cv2.FONT_HERSHEY_SIMPLEX,
                1.2, color, 3)

    return frame