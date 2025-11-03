//
//  RealPreviewView.swift
//  ReactNativeCameraKit
//

import AVFoundation

class RealPreviewView: UIView {
    // Use AVCaptureVideoPreviewLayer as the view's backing layer.
    override class var layerClass: AnyClass {
        AVCaptureVideoPreviewLayer.self
    }

    // Create an accessor for the right layer type
    var previewLayer: AVCaptureVideoPreviewLayer {
        // We can safely forcecast here, it can't change at runtime
        // swiftlint:disable:next force_cast
        return layer as! AVCaptureVideoPreviewLayer
    }

    // Connect the layer to a capture session.
    private var _session: AVCaptureSession?
    var session: AVCaptureSession? {
        get { _session }
        set {
            guard _session !== newValue else { return }
            _session = newValue
            DispatchQueue.main.async {
                self.previewLayer.session = newValue
            }
        }
    }
}
