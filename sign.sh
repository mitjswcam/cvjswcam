#!/bin/sh
jarsigner -keystore /home/webcam/.keystore/webcamKeystore -storepass mypassword -keypass mypassword /home/webcam/workspace/m_webcam/target/m_webcam-1.0-jar-with-dependencies.jar MIT-Webcam
