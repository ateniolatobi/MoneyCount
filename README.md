
# MONEYCOUNT

## Description

- MoneyCount is an opensource Mobile Application that utilizes Artificial Intelligence and the camera of a mobile device to scan images of the Naira banknote to recognize, classify and then give audio feedback to the user before summing up the denominations. This is to use the power of Deep learning to give Visually Impaired people in Nigeria the opportunity to count money by themselves to address the issue in Nigeria where people living with visual Impairment get scammed and swindled by other people when asking for assistance to use the ATM, count money or shopping as a result of their disability. 

-  MoneyCount is powered by a Deep Learning Model called Nairanet. Nairanet is derived from the Mobilenet architecture because of its relatively small size and good accuracy. The model was created and trained in PyTorch and then converted to the pytorch mobile format to be easily integrated into a mobile application.

- To obtain the training and test dataset I used in training the classifier and for validation, 3 main resources were utilized. 
    - I took pictures of the different denominations of the Naira note by myself
    - I collected images from Dataset donors  
    - I also downloaded images from the internet.
- To ensure the privacy protection of the Dataset contributors and the users of the application, I made use of the following privacy protection mechanisms. 
  - I anonymized the dataset by changing the names of all the images and replacing them with indexes. 
  - I applied Local Differential Privacy by randomly mixing my Images and also the Images I got from the Internet with the Images I got from Data donors to protect against Differencing Attacks. 
  - The model exists offline on the application. This ensures that no internet connection would be needed for the use of the application and also ensures that when the users use the application no data is updated or sent to the internet or any database. This offers privacy protection against cyberattacks and data theft. 


-  As a result of the limited number of Datasets, I performed different data augmentation techniques such as Horizontal flipping, Vertical flipping, cropping e.t.c. To augment the dataset to improve learning.

- The Android application was written in JAVA. The Android Camera 2 API is used to integrate the camera view of the smartphone into the application, autofocus when an object is detected, and also to take pictures.
- The neural network(nairanet)  for the image classification was created and stored using pytorch mobile format. The neural network exists in the assets directory in the Android application folder.
- The application goes with a minimalist and simplistic approach in regards to its UI/UX to enable people with visual impairment to use the application efficiently. Audio instructions on the placement and use of the buttons are given at the launch of the application. The "inspect" and "sum" buttons are made big enough to enable it to be easier to click on and also at each click, audio feedback is given to inform the user if it detected a click, what button got clicked on, if it scanned successfully, the amount scanned and the sum.

- Due to the large size of the train and test dataset, the dataset is not uploaded on Github but instead can be downloaded from the google drive link below. [Google Drive link for the datasets](https://drive.google.com/drive/folders/12RHPVx28-LQcylPyZQOAKEptui9lu28x?usp=sharing)

---
## PROPOSED IMPROVEMENTS

- The model used in the application has relatively good accuracy in the detection and classification of the banknotes but a larger dataset would be needed to train a model that would have better accuracy. A website for database crowd-sourcing is proposed to be set up to allow people to donate images so that the model can be retrained and improved.
- Federated learning is proposed to be integrated into the application. We can implement Federated learning by making the application retrain the neural network at each use of the application by the user. When an internet connection is detected and the application is being used, the gradients gotten through federated learning from the use of the application would be sent to the application server where they would be used to update the central model. By using federated learning, we can better protect the privacy of the application users and also improve the accuracy of the model.
 - Application updates would improve the accuracy of the neural network and also include added features without involving any exchange of data with the users.
 - The application can also be extended to have other features such as:-
     - Detecting and classifying other banknotes apart from the naira. 
     - Detecting when an object in focus is not a banknote.
     - Detecting and working with the digital and cashless mode of payments such as credit cards, receipts, automated teller machines, POS machines.
     
- The application can be made cross-platform for different mobile operating systems. 


## Application Screenshots


<img src="https://github.com/ateniolatobi/MoneyCount/blob/master/assets/5.jpg" width="40%">
<img src="https://github.com/ateniolatobi/MoneyCount/blob/master/assets/10.jpg" width="40%">
<img src="https://github.com/ateniolatobi/MoneyCount/blob/master/assets/20.jpg" width="40%">
<img src="https://github.com/ateniolatobi/MoneyCount/blob/master/assets/50.jpg" width="40%">
<img src="https://github.com/ateniolatobi/MoneyCount/blob/master/assets/100.jpg" width="40%">
<img src="https://github.com/ateniolatobi/MoneyCount/blob/master/assets/1000.jpg" width="40%">
<img src="https://github.com/ateniolatobi/MoneyCount/blob/master/assets/total.jpg" width="40%">




## Model Description

Nairanet is derived from a modified pre-trained mobilenet v2 architecture. The structure of Nairanet and the weights can be obtained in the project repository. The training data can also be accessed with the Google drive link above.



<img src="https://github.com/ateniolatobi/MoneyCount/blob/master/assets/MobileNet.png" width="100%">



---



## Tools Used

> The model was created and trained with PyTorch but then converted to Pytorch mobile format for easier integration in a mobile application. Details about the use of Pytorch and Pytorch mobile would be given in the Reference section below.


- torch==1.1.0
- torchsummary==1.5.1
- torchtext==0.3.1
- torchvision==0.3.0
- numpy==1.16.4
- matplotlib==3.0.3




```

pip install torch

pip install pandas

pip install numpy 

pip install matplotlib

```



## References

1. [Discription of PyTorch models](https://pytorch.org/docs/stable/torchvision/models.html)

2. [How to convert a PyTorch model to Pytorch mobile for easier implementation into an Android Application](https://pytorch.org/mobile/android/)

3. [Explanation of the MobileNet V2 Architecture](https://machinethink.net/blog/mobilenet-v2/)

4. [Youtube Tutorial on how to implement a camera view into an android application through the Camera2 api](https://www.youtube.com/watch?v=CuvVpsFc77w&list=PL9jCwTXYWjDIHNEGtsRdCTk79I9-95TbJ)
5. [Pytorch Beginners Tutorial](https://pytorch.org/tutorials/)
