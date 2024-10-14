import React from 'react';
import { requireNativeComponent, NativeModules } from 'react-native';
const { CKCameraManager } = NativeModules;
const NativeCamera = requireNativeComponent('CKCamera');
const Camera = React.forwardRef((props, ref) => {
    const nativeRef = React.useRef(null);
    props.resetFocusTimeout = props.resetFocusTimeout ?? 0;
    props.resetFocusWhenMotionDetected = props.resetFocusWhenMotionDetected ?? true;
    React.useImperativeHandle(ref, () => ({
        capture: async () => {
            return await CKCameraManager.capture({});
        },
        requestDeviceCameraAuthorization: async () => {
            return await CKCameraManager.checkDeviceCameraAuthorizationStatus();
        },
        checkDeviceCameraAuthorizationStatus: async () => {
            return await CKCameraManager.checkDeviceCameraAuthorizationStatus();
        },
    }));
    return <NativeCamera style={{ minWidth: 100, minHeight: 100 }} ref={nativeRef} {...props}/>;
});
export default Camera;
//# sourceMappingURL=Camera.ios.js.map