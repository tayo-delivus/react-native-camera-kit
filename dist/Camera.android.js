import React from 'react';
import { requireNativeComponent, findNodeHandle, NativeModules, processColor } from 'react-native';
const { RNCameraKitModule } = NativeModules;
const NativeCamera = requireNativeComponent('CKCameraManager');
const Camera = React.forwardRef((props, ref) => {
    const nativeRef = React.useRef(null);
    React.useImperativeHandle(ref, () => ({
        capture: async (options = {}) => {
            return await RNCameraKitModule.capture(options, findNodeHandle(nativeRef.current ?? null));
        },
        requestDeviceCameraAuthorization: () => {
            throw new Error('Not implemented');
        },
        checkDeviceCameraAuthorizationStatus: () => {
            throw new Error('Not implemented');
        },
    }));
    const transformedProps = { ...props };
    transformedProps.ratioOverlayColor = processColor(props.ratioOverlayColor);
    transformedProps.frameColor = processColor(props.frameColor);
    transformedProps.laserColor = processColor(props.laserColor);
    return <NativeCamera style={{ minWidth: 100, minHeight: 100 }} ref={nativeRef} {...transformedProps}/>;
});
export default Camera;
//# sourceMappingURL=Camera.android.js.map