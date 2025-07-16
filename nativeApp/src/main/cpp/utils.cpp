#include "utils.h"

namespace PowerSync {
std::string create_random_id() {
  static const char alphanum[] = "0123456789"
                                 "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                 "abcdefghijklmnopqrstuvwxyz";
  int len = 16;
  std::string id;
  for (int i = 0; i < len; ++i) {
    id += alphanum[rand() % (sizeof(alphanum) - 1)];
  }
  return id;
}

} // namespace PowerSync