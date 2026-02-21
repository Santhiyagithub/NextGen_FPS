def classify_frame(timestamp_flag, motion, merge_flag, score):

    if timestamp_flag == "DROP":
        return "Frame Drop"

    if score < 0.6:
        return "Frame Merge"

    if merge_flag and motion < 0.6:
        return "Frame Merge"

    return "Normal"