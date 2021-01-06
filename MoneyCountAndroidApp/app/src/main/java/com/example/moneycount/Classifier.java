package com.example.moneycount;

import android.graphics.Bitmap;
import org.pytorch.Tensor;
import org.pytorch.Module;
import org.pytorch.IValue;
import org.pytorch.torchvision.TensorImageUtils;

// nairanet model for classifying Images.
public class Classifier {

    Module model;
    float[] mean = {0.485f, 0.456f, 0.406f}; // mean for normalization
    float[] std = {0.229f, 0.224f, 0.225f}; // std for normalization.

    public Classifier(String modelPath){

        model = Module.load(modelPath); // Loads modelpath

    }

    public void setMeanAndStd(float[] mean, float[] std){

        this.mean = mean;
        this.std = std;
    }

    // preprocesses the bitmap image into the size in order to run in the model.
    public Tensor preprocess(Bitmap bitmap, int size){
        bitmap = Bitmap.createScaledBitmap(bitmap,size,size,false); // scale the bitmap using the size without applying filters.
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap,this.mean,this.std); // normalizes the Image using the mean and std.

    }

    // argMax() takes the distributed probability score accross the different classes and returns the
    // index of the max probability score.
    public int argMax(float[] inputs){

        int maxIndex = -1;
        float maxvalue = 0.0f;
        for (int i = 0; i < inputs.length; i++){
            if(inputs[i] > maxvalue) {
                maxIndex = i;
                maxvalue = inputs[i];
            }
        }
        return maxIndex;
    }

    // predict(), returns the predicted amount.
    public String predict(Bitmap bitmap){

        Tensor tensor = preprocess(bitmap,224); // uses the preprocess method to resize and normalize the image.

        IValue inputs = IValue.from(tensor); // converts from tensor to IValue
        Tensor outputs = model.forward(inputs).toTensor(); // runs the input through the model and convert the result to tensor
        float[] scores = outputs.getDataAsFloatArray(); // converts from tensort to a floatarray.

        int classIndex = argMax(scores); // gets the index of the maximum probability score.

        return Constants.IMAGENET_CLASSES[classIndex]; // returns the equivalent  predicted amount.

    }

}