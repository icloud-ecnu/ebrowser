# eBrowser---An energy-efficient mobile Web interaction framework in mobile Web browsers

ebrowser, an energy-efficient and lightweight human interaction framework without degrading the user interaction experience in mobile Web browsers.

## Architecture of ebrowser

ebrowser comprises the remote cloud side and the local browser side. The cloud side leverages the user interaction data to train the personalized event rate model for each mobile device using the SVR technique. The browser side collects the user interaction data and controls its interaction event rate, in order to reduce the power consumption of mobile Web interactions without degrading the interaction experience.

![](https://github.com/ebrowser-cloud/ebrowser/raw/master/images/architecture.png)  

## Setup,Compiling and Configuration

The browser side of ebrowser framework is developed base on [The Chromium Projects](https://www.chromium.org). The project version is 56.

### 1.External Dependencies

Before using our framework, you should get the code of Chromium for Android first. The instructions for checking out, building,& running Chromium is [here](https://chromium.googlesource.com/chromium/src/+/master/docs/android_build_instructions.md).

### 2.Compiling

Replace content & ui compotents in src/ directory of Chromium project with ebrowser-cloud/ebrowser/content & ui. Build [Content shell](https://chromium.googlesource.com/chromium/src/+/master/docs/android_build_instructions.md#Build-Content-shell) as the browser side of eBrowser.

```
ninja -C out/Default content_shell_apk
out/Default/bin/content_shell_apk instal
```
### 3.Deploying the cloud side
Just deploy it as any other Spring project.

# Publication
Fei Xu, Shuai Yang, Zhi Zhou, Jia Rao, “eBrowser: Making Human-Mobile Web Interactions Energy Efficient with Event Rate Learning,” in: Proc. of ICDCS 2018 (Research Track), Vienna, Austria, July 2-5, 2018.
