#ifndef POWERSYNCANDROIDEXAMPLE_OKHTTP_H
#define POWERSYNCANDROIDEXAMPLE_OKHTTP_H

#include <string>

namespace PowerSync {
    std::string get(const std::string& url);
    std::string postJson(const std::string& url, const std::string& body);
} // PowerSync

#endif //POWERSYNCANDROIDEXAMPLE_OKHTTP_H
