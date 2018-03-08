# eBrowser---An energy-efficient mobile Web interaction framework in mobile Web browsers

ebrowser, an energy-efficient and lightweight human interaction framework without degrading the user interaction experience in mobile Web browsers.

## Architecture of ebrowser

ebrowser comprises the remote cloud side and the local browser side. The cloud side leverages the user interaction data to train the personalized event rate model for each mobile device using the SVR technique. The browser side collects the user interaction data and controls its interaction event rate, in order to reduce the power consumption of mobile Web interactions without degrading the interaction experience.

![](https://github.com/ebrowser-cloud/ebrowser/raw/master/images/architecture.png)  
