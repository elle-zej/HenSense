import os
import tensorflow as tf
import numpy as np
from fastapi import FastAPI, File, UploadFile
from inference_sdk import InferenceHTTPClient
from dotenv import load_dotenv
from io import BytesIO
import uvicorn

# Load environment variables
load_dotenv()
API_KEY = os.getenv("ROBO_API_KEY")

# Initialize Roboflow client
CLIENT = InferenceHTTPClient(
    api_url="https://detect.roboflow.com",
    api_key=API_KEY
)

app = FastAPI()

@app.get("/")
def read_root():
    print("Root endpoint was hit")  # Debugging log
    return {"message": "HenSense API is running"}

@app.get("/favicon.ico")
async def favicon():
    return {}

# Function to read image file and preprocess it
def read_image(image_data):
    image = tf.image.decode_jpeg(image_data, channels=3).numpy()
    return image

@app.post("/predict/")
async def predict(file: UploadFile = File(...)):
    # Read file and convert to numpy array
    image_data = await file.read()
    print("Image data type: ", type(image_data))
    print("Image data: ", image_data[:100])  # Print only first 100 bytes to avoid spam

    # Perform inference
    result = CLIENT.infer(image_data, model_id="healthy-and-sick-chicken-detection-kavqw/18")

    return {"prediction": result}

# Run the FastAPI server
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
