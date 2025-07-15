#pragma once

#ifndef POWERSYNCANDROIDEXAMPLE_POWERSYNCCONNECTOR_H
#define POWERSYNCANDROIDEXAMPLE_POWERSYNCCONNECTOR_H

#include <functional>
#include <jni.h>

namespace PowerSync {

typedef std::function<std::tuple<std::string, std::string>()>
    CredentialsCallback;

typedef std::function<std::string(const std::string&, const std::string&, const std::string&)>
    UploadCallback;

class Connector {
public:
  Connector(CredentialsCallback credentials_callback, UploadCallback upload_callback);
  jobject getHandle();
  CredentialsCallback credentials_callback;
  UploadCallback upload_callback;

private:
  jobject handle;
};

} // namespace PowerSync

#endif // POWERSYNCANDROIDEXAMPLE_POWERSYNCCONNECTOR_H
