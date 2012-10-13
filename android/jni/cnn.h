#ifndef _CNN_H_INCLUDED
#define _CNN_H_INCLUDED

#include <iostream>
#include <fstream>
#include <vector>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/objdetect/objdetect.hpp>

#include "cvconvnet.h"

class Cnn
{
    cv::CascadeClassifier mFaceCascade;
    CvConvNet mConvNet;

    public:
        Cnn ( );
        virtual ~Cnn ( );
        bool loadCascade(const char*);
        bool loadConvNet(const char*);
        std::vector<cv::Rect> findFaces(cv::Mat const);
        void drawRectangles(std::vector<cv::Rect>, cv::Mat);
        cv::Mat cropFrame(cv::Mat, cv::Rect);
        double runConvNet(cv::Mat const);
};
#endif // _CNN_H_INCLUDED
