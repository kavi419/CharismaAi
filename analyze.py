import sys
import json
import cv2
import os
import numpy as np
from moviepy import VideoFileClip
import speech_recognition as sr

def analyze_video(video_path):
    results = {
        "eye_contact_score": 0,
        "audio_score": 0,
        "pause_count": 0,
        "overall_score": 0,
        "feedback": "Analysis failed.",
        "transcript": "",
        "filler_count": 0
    }

    try:
        # --- Video Analysis (Eye Contact) ---
        cap = cv2.VideoCapture(video_path)
        
        # Load Haar Cascade for face detection
        face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')

        total_frames = 0
        eye_contact_frames = 0

        while cap.isOpened():
            success, image = cap.read()
            if not success:
                break

            total_frames += 1

            # Convert to grayscale for Haar Cascade
            gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
            faces = face_cascade.detectMultiScale(gray, 1.1, 4)

            # Simplified Logic: If a face is detected, we assume "Eye Contact" (looking at camera)
            if len(faces) > 0:
                eye_contact_frames += 1

        cap.release()

        # Calculate Eye Contact Score
        eye_contact_score = 0
        if total_frames > 0:
            eye_contact_score = int((eye_contact_frames / total_frames) * 100)

        results["eye_contact_score"] = eye_contact_score

        # --- Audio Analysis (Fluency, Pauses, Transcript) ---
        audio_score = 100
        pause_count = 0
        transcript = ""
        filler_count = 0
        temp_audio = "temp_audio.wav"

        try:
            clip = VideoFileClip(video_path)
            if clip.audio is not None:
                # Write temp audio file
                clip.audio.write_audiofile(temp_audio, logger=None)
                
                # 1. Fluency & Pauses (Signal Processing)
                fps = 44100
                audio_array = clip.audio.to_soundarray(fps=fps)
                if audio_array.ndim == 2:
                    audio_array = audio_array.mean(axis=1) # Convert to mono
                
                max_val = np.abs(audio_array).max()
                if max_val > 0:
                    audio_array = audio_array / max_val
                
                threshold = 0.05
                is_silent = np.abs(audio_array) < threshold
                
                min_pause_samples = fps # 1 second
                current_pause_length = 0
                for silent in is_silent:
                    if silent:
                        current_pause_length += 1
                    else:
                        if current_pause_length > min_pause_samples:
                            pause_count += 1
                        current_pause_length = 0
                
                if current_pause_length > min_pause_samples:
                    pause_count += 1

                # 2. Speech Recognition & Filler Detection
                recognizer = sr.Recognizer()
                with sr.AudioFile(temp_audio) as source:
                    audio_data = recognizer.record(source)
                    try:
                        transcript = recognizer.recognize_google(audio_data)
                    except sr.UnknownValueError:
                        transcript = "(Speech not recognized)"
                    except sr.RequestError:
                        transcript = "(API unavailable)"

                # Count Filler Words
                filler_words = ["um", "uh", "ah", "like", "you know", "mean"]
                lower_transcript = transcript.lower()
                for word in filler_words:
                    # Simple count (could be improved with regex validation tokens)
                    filler_count += lower_transcript.count(word)

                # Calculate Audio Score
                # Start 100, deduct for pauses and fillers
                deduction = (pause_count * 5) + (filler_count * 3)
                audio_score = max(0, 100 - deduction)

                # Cleanup
                clip.close()
                if os.path.exists(temp_audio):
                    os.remove(temp_audio)
            else:
                audio_score = 0
                clip.close()

        except Exception as e:
            audio_score = 0
            transcript = "Error capturing audio: " + str(e)
            if os.path.exists(temp_audio):
                os.remove(temp_audio)

        results["audio_score"] = int(audio_score)
        results["pause_count"] = pause_count
        results["transcript"] = transcript
        results["filler_count"] = filler_count

        # --- Final Scoring & Feedback ---
        results["overall_score"] = int((eye_contact_score + audio_score) / 2)

        if filler_count > 5:
             results["feedback"] = f"Detected {filler_count} filler words. Try to be more concise."
        elif pause_count > 3:
            results["feedback"] = "Try to reduce long pauses to sound more fluent."
        elif eye_contact_score < 50:
            results["feedback"] = "Focus on looking at the camera."
        else:
            results["feedback"] = "Great job! Good flow and eye contact."

    except Exception as e:
        results["feedback"] = "Error during analysis: " + str(e)

    # Return JSON
    print(json.dumps(results))

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No video file provided"}))
    else:
        video_path = sys.argv[1]
        analyze_video(video_path)
