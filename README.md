# RateMe Android app

* [Overview](#overview)
* [UI](#ui)
* [Backend logic](#backend-logic)


## Overview

This repo provides the information how to integrate android application. A client can integrate fully by utilizing already implemented UI or use a manager class, in headless mode, that will communicate with [Least-backend service](https://github.com/codingoperations/least-service). 

**NOTE**: Before integrating android application, least backend service must be configured by:
- Generating API token. It will be used to authenticate android app with the backend service
- Providing pop-up message description
- Tag messages
- Rate limit

Please refer to [least-service](https://github.com/codingoperations/least-service) documentation of how to create proper configuration. 

## UI

Sample UI already includes capability

[<img src="img.png" width="250"/>](images/img.png)
[<img src="img_1.png" width="250"/>](images/img_1.png)
[<img src="img_2.png" width="250"/>](images/img_2.png)

## Backend logic


