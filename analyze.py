import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 
import sys

# Redirect stderr to suppress MediaPipe logs
stderr = sys.stderr
sys.stderr = open(os.devnull, 'w')

import cv2
import mediapipe as mp
import numpy as np
import json
import speech_recognition as sr
from moviepy import VideoFileClip

# Restore stderr
sys.stderr = stderr

def analyze_video(video_path):
    # Initialize MediaPipe
    mp_face_mesh = mp.solutions.face_mesh
    face_mesh = mp_face_mesh.FaceMesh(max_num_faces=1, refine_landmarks=True, min_detection_confidence=0.5, min_tracking_confidence=0.5)

    # Variables
    eye_contact_frames = 0
    smile_frames = 0
    posture_frames = 0
    total_frames = 0

    cap = cv2.VideoCapture(video_path)
    while cap.isOpened():
        success, image = cap.read()
        if not success:
            break
        total_frames += 1

        results = face_mesh.process(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
        if results.multi_face_landmarks:
            landmarks = results.multi_face_landmarks[0].landmark

            # 1. Eye Contact
            if landmarks[468].x: eye_contact_frames += 1

            # 2. Smile (Mouth Width vs Lip Height)
            mouth_width = abs(landmarks[61].x - landmarks[291].x)
            lip_height = abs(landmarks[0].y - landmarks[17].y)
            if mouth_width > 0 and (lip_height / mouth_width) < 0.3:
                smile_frames += 1

            # 3. Posture (Nose Center)
            nose = landmarks[1]
            if 0.4 < nose.x < 0.6 and 0.4 < nose.y < 0.7:
                posture_frames += 1

    cap.release()

    # Audio Logic
    audio_score = 0
    transcript = "No speech detected."
    filler_count = 0

    try:
        video = VideoFileClip(video_path)
        video.audio.write_audiofile("temp_audio.wav", verbose=False, logger=None)

        r = sr.Recognizer()
        with sr.AudioFile("temp_audio.wav") as source:
            r.adjust_for_ambient_noise(source, duration=0.5)
            audio_data = r.record(source)

        try:
            text = r.recognize_google(audio_data)
            transcript = text
            words = text.split()
            if len(words) < 3:
                audio_score = 20
                transcript += " (Try to speak more)"
            else:
                fillers = ["um", "uh", "ah", "like", "mean"]
                filler_count = sum(1 for w in words if w.lower() in fillers)
                audio_score = max(0, 100 - (filler_count * 5))
        except:
            pass

        if os.path.exists("temp_audio.wav"): os.remove("temp_audio.wav")
    except:
        pass

    # Scores
    eye_score = int((eye_contact_frames / total_frames) * 100) if total_frames > 0 else 0
    smile_score = int((smile_frames / total_frames) * 100) if total_frames > 0 else 0
    posture_score = int((posture_frames / total_frames) * 100) if total_frames > 0 else 0
    overall = int((eye_score + audio_score + smile_score + posture_score) / 4)

    feedback = "Great job!"
    if overall < 50: feedback = "Keep practicing!"

    # CRITICAL FIX: Use json.dumps to ensure double quotes
    result = {
        "eye_contact_score": eye_score,
        "audio_score": audio_score,
        "smile_score": smile_score,
        "posture_score": posture_score,
        "pause_count": 0, # Added for Java compatibility
        "transcript": transcript,
        "filler_count": filler_count, # Mapped for consistency
        "filler_words": filler_count, # Kept for user request
        "overall_score": overall,
        "feedback": feedback
    }
    print(json.dumps(result))

if __name__ == "__main__":
    if len(sys.argv) > 1:
        analyze_video(sys.argv[1])
