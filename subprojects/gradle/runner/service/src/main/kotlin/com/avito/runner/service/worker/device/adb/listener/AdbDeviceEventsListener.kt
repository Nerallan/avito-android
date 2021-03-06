package com.avito.runner.service.worker.device.adb.listener

import com.avito.runner.service.worker.device.Device
import java.nio.file.Path

interface AdbDeviceEventsListener {

    fun onGetSdkPropertySuccess(attempt: Int, api: Int)
    fun onGetSdkPropertyAttemptFail(attempt: Int)
    fun onGetSdkPropertyFailure(throwable: Throwable)

    fun onInstallApplicationSuccess(device: Device, attempt: Int, applicationPackage: String)
    fun onInstallApplicationAttemptFail(device: Device, attempt: Int, applicationPackage: String)
    fun onInstallApplicationFailure(device: Device, applicationPackage: String, throwable: Throwable)

    fun onGetAliveDeviceSuccess(device: Device, attempt: Int)
    fun onGetAliveDeviceAttemptFail(device: Device, attempt: Int)
    fun onGetAliveDeviceFailed(device: Device, throwable: Throwable)

    fun onClearPackageSuccess(device: Device, attempt: Int, name: String)
    fun onClearPackageAttemptFail(device: Device, attempt: Int, name: String, throwable: Throwable)
    fun onClearPackageFailure(device: Device, name: String, throwable: Throwable)

    fun onPullSuccess(device: Device, from: Path, to: Path)
    fun onPullAttemptFail(device: Device, attempt: Int, from: Path, throwable: Throwable)
    fun onPullFailure(device: Device, from: Path, throwable: Throwable)

    fun onClearDirectorySuccess(device: Device, remotePath: Path, output: String)
    fun onClearDirectoryNothingDone(device: Device, remotePath: Path)
    fun onClearDirectoryAttemptFail(device: Device, attempt: Int, remotePath: Path, throwable: Throwable)
    fun onClearDirectoryFailure(device: Device, remotePath: Path, throwable: Throwable)

    fun onListSuccess(device: Device, remotePath: String)
    fun onListAttemptFail(device: Device, attempt: Int, remotePath: String, throwable: Throwable)
    fun onListFailure(device: Device, remotePath: String, throwable: Throwable)

    fun onRunTestPassed(device: Device, testName: String)
    fun onRunTestIgnored(device: Device, testName: String)
    fun onRunTestRunError(device: Device, testName: String, errorMessage: String)
    fun onRunTestInfrastructureError(device: Device, testName: String, errorMessage: String, throwable: Throwable?)
    fun onRunTestFailedOnStart(device: Device, message: String)
    fun onRunTestFailedOnInstrumentationParse(device: Device, message: String, throwable: Throwable)
}
