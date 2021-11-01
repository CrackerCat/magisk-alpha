#pragma once

#include <pthread.h>
#include <string_view>
#include <functional>
#include <map>
#include <atomic>

#include <daemon.hpp>

#define SIGTERMTHRD SIGUSR1
#define ISOLATED_MAGIC "isolated"

// CLI entries
int enable_deny();
int disable_deny();
int add_list(int client);
int rm_list(int client);
void ls_list(int client);

// Utility functions
bool is_deny_target(int uid, std::string_view process, int max_len = 1024);
void crawl_procfs(const std::function<bool(int)> &fn);

// Process monitoring
extern pthread_t monitor_thread;
void proc_monitor();


// Revert
void revert_daemon(int pid);
void revert_unmount(int pid = -1);

// Props
void hide_sensitive_props();

extern std::atomic<bool> denylist_enforced;
extern std::atomic<int> cached_manager_app_id;

enum : int {
    ENFORCE_DENY,
    DISABLE_DENY,
    ADD_LIST,
    RM_LIST,
    LS_LIST,
    DENY_STATUS,
};

enum : int {
    DENY_IS_ENFORCED = DAEMON_LAST + 1,
    DENY_NOT_ENFORCED,
    DENYLIST_ITEM_EXIST,
    DENYLIST_ITEM_NOT_EXIST,
    DENYLIST_INVALID_PKG,
    DENY_NO_NS,
};
