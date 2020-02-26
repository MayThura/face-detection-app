#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/objdetect.hpp>

using namespace std;
using namespace cv;

extern "C"


JNIEXPORT void JNICALL
Java_com_example_user_facedetectionapp_MainActivity_processImage(
        JNIEnv *env,
        jobject /* this */, jobject buffer, jint width, jint height, jobject bitmap) {

    String face_cascade_name = "/mnt/sdcard/haarcascade_frontalface_alt.xml";
    String eyes_cascade_name = "/mnt/sdcard/haarcascade_eye_tree_eyeglasses.xml";

    CascadeClassifier face_cascade;
    CascadeClassifier eyes_cascade;

    if( !face_cascade.load( face_cascade_name ) )
    {
        printf("--(!)Error loading\n");
        return ;
    };
    if( !eyes_cascade.load( eyes_cascade_name ) )
    {
        printf("--(!)Error loading\n");
        return ;
    };

    unsigned char* inputPtr = (unsigned char*)env->GetDirectBufferAddress(buffer);
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env,bitmap,&info);
    unsigned char* target;
    AndroidBitmap_lockPixels(env,bitmap,(void**)&(target));

    cv::Mat temp;
    cv::Mat image(cv::Size(info.width,info.height),CV_8UC4, target);
    cv::Mat yuv(info.height+info.height/2, info.width, CV_8UC1, (uchar *)inputPtr);
    cv::cvtColor(yuv, image, CV_YUV2RGBA_NV21);

    std::vector<Rect> faces;
    Mat frame_gray;

    cvtColor( image, frame_gray, COLOR_BGR2GRAY );
    equalizeHist( frame_gray, frame_gray );

    //-- Detect faces
    face_cascade.detectMultiScale( frame_gray, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );

    for ( size_t i = 0; i < faces.size(); i++ )
    {
        Point center( faces[i].x, faces[i].y );
        rectangle( image, center, Size( faces[i].width*2, faces[i].height*2 ), Scalar( 255, 0, 255 ), 4, 8, 0 );

        Mat faceROI = frame_gray( faces[i] );
        std::vector<Rect> eyes;

        //-- In each face, detect eyes
        eyes_cascade.detectMultiScale( faceROI, eyes, 1.1, 2, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );

        for ( size_t j = 0; j < eyes.size(); j++ )
        {
            Point eye_center( faces[i].x + eyes[j].x + eyes[j].width/2, faces[i].y + eyes[j].y + eyes[j].height/2 );
            int radius = cvRound( (eyes[j].width + eyes[j].height)*0.25 );
            circle( image, eye_center, radius, Scalar( 255, 0, 0 ), 4, 8, 0 );
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

