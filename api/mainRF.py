import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import joblib
import pandas as pd
import logging
import traceback

# Load the trained model and scaler
current_dir = os.getcwd()
model_path = os.path.join(current_dir, "RBTwin_model.pkl")
scaler_path = os.path.join(current_dir, "scaler.pkl")

model = joblib.load(model_path)
scaler = joblib.load(scaler_path)

# Create FastAPI app
app = FastAPI()

# Set up logging for better error visibility
logging.basicConfig(level=logging.DEBUG)

# Define the input schema
class TimeSeriesInput(BaseModel):
    data: List[List[float]]  # Raw sensor data in a 2D array

# Normalize data using the MinMaxScaler
def normalize_data(data: pd.DataFrame, scaler) -> pd.DataFrame:
    """Normalize the test data using the provided scaler."""
    try:
        normalized = scaler.transform(data)
        return pd.DataFrame(normalized, columns=data.columns)
    except Exception as e:
        logging.error(f"Error normalizing data: {str(e)}")
        raise HTTPException(status_code=500, detail="Error normalizing data")

# Extract features from a sliding window
def extract_features(window: pd.DataFrame) -> dict:
    """Extract statistical features from a window of data."""
    features = {}
    for col in window.columns:
        try:
            if len(window[col].unique()) > 1:  # Avoid constant columns
                features[f'{col}_mean'] = window[col].mean()
                features[f'{col}_std'] = window[col].std()
                features[f'{col}_min'] = window[col].min()
                features[f'{col}_max'] = window[col].max()
            else:
                # For constant columns
                features[f'{col}_mean'] = window[col].mean()
                features[f'{col}_std'] = 0
                features[f'{col}_min'] = window[col].min()
                features[f'{col}_max'] = window[col].max()
        except Exception as e:
            logging.error(f"Error extracting feature for column {col}: {str(e)}")
            raise HTTPException(status_code=500, detail=f"Error extracting feature for column {col}")
    return features

@app.post("/predict/")
async def predict(timeseries: TimeSeriesInput):
    try:
        # Convert the input data to a pandas DataFrame
        raw_data = pd.DataFrame(timeseries.data, columns=[
            "acc_X", "acc_Y", "acc_Z", "mag_X", "mag_Y", "mag_Z", "gyro_X", "gyro_Y", "gyro_Z"
        ])

        # Validate input dimensions
        if len(raw_data) < 50:
            raise HTTPException(status_code=400, detail="The input time series must have at least 50 rows.")

        logging.info(f"Received data with {len(raw_data)} rows.")

        # Normalize the input data before feature extraction
        normalized_data = normalize_data(raw_data, scaler)

        # Extract features from the window (last 50 rows)
        window = normalized_data.iloc[-50:]  # Take the last 50 rows
        features = extract_features(window)
        features_df = pd.DataFrame([features])  # Convert to DataFrame

        logging.info(f"Extracted features: {features}")

        # Ensure the columns match the ones the model expects
        expected_columns = [
            "acc_X_mean", "acc_X_std", "acc_X_min", "acc_X_max",
            "acc_Y_mean", "acc_Y_std", "acc_Y_min", "acc_Y_max",
            "acc_Z_mean", "acc_Z_std", "acc_Z_min", "acc_Z_max",
            "mag_X_mean", "mag_X_std", "mag_X_min", "mag_X_max",
            "mag_Y_mean", "mag_Y_std", "mag_Y_min", "mag_Y_max",
            "mag_Z_mean", "mag_Z_std", "mag_Z_min", "mag_Z_max",
            "gyro_X_mean", "gyro_X_std", "gyro_X_min", "gyro_X_max",
            "gyro_Y_mean", "gyro_Y_std", "gyro_Y_min", "gyro_Y_max",
            "gyro_Z_mean", "gyro_Z_std", "gyro_Z_min", "gyro_Z_max"
        ]

        # Add any missing columns with default values (to handle the case where a feature is missing)
        for col in expected_columns:
            if col not in features_df.columns:
                features_df[col] = 0

        # Ensure the columns are in the correct order
        features_df = features_df[expected_columns]

        # Predict the label using the trained model
        prediction = model.predict(features_df)
        predicted_label = prediction[0]

        # Map the string label to an integer (if necessary)
        label_mapping = {
            "downstairs": 0,
            "upstairs": 1,
            "walking": 2,
            "running": 3,
            "standing": 4
        }

        # Check if predicted_label is a string and map it
        if isinstance(predicted_label, str) and predicted_label in label_mapping:
            predicted_label = label_mapping[predicted_label]

        logging.info(f"Prediction: {predicted_label}")

        return {"predicted_label": int(predicted_label)}

    except Exception as e:
        logging.error(f"Error in prediction: {traceback.format_exc()}")
        raise HTTPException(status_code=500, detail="Prediction failed")

@app.get("/helloworld/")
async def hello_world():
    return {"message": "Hello, world!"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
