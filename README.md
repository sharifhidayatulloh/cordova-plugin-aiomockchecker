# cordova-plugin-applaudmockchecker

This is a cordova plugin to avoid mock locations

This plugin get mock location in Android api <= 22 AND api > 22

## Supported Platforms

- Android API all versions

## Installation

```bash
cordova plugin add cordova-plugin-applaudmockchecker
```

## Usage in javascript

```js
document.addEventListener("deviceready", onDeviceReady, false);

function onDeviceReady() {
  mockchecker.check(successCallback, errorCallback);
}

function successCallback(mockStatus) {
  console.log(mockStatus);
}

function errorCallback(error) {
  console.log(error);
}
```

## mockStatus

Contains mock status :

### properties

- isMock : (boolean) true if device mock, false if no mock behavior detected.
- title : (string) returns title if device using mock locartion or developer options are enabled.
- messages : (string) this properties exists if isMock properties values is true.
- osVersion : (int) return platform OS version.
