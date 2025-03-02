import os
from fastapi import FastAPI, HTTPException
from inference_sdk import InferenceHTTPClient
from dotenv import load_dotenv
import uvicorn
from pydantic import BaseModel

# Load environment variables
load_dotenv()
API_KEY = os.getenv("ROBO_API_KEY")

# Initialize Roboflow client
CLIENT = InferenceHTTPClient(
    api_url="https://detect.roboflow.com",
    api_key=API_KEY
)

app = FastAPI()

# Define the request model
class PredictionRequest(BaseModel):
    url: str

@app.get("/")
def read_root():
    print("Root endpoint was hit")  # Debugging log
    return {"message": "HenSense API is running"}

@app.get("/favicon.ico")
async def favicon():
    return {}

@app.post("/predict/")
async def predict(request: PredictionRequest):
    try:
        # Perform inference using the Roboflow client
        result = CLIENT.infer(request.url, model_id="healthy-and-sick-chicken-detection-kavqw/18")
        return {"prediction": result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Run the FastAPI server
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)