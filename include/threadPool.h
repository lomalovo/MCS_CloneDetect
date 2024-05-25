#ifndef THREADPOOL_H
#define THREADPOOL_H

#include <thread>
#include <mutex>
#include <queue>
#include <condition_variable>
#include <functional>


class ThreadPool {
public:
    ThreadPool(std::size_t num_threads);
    ~ThreadPool();
    void enqueue(std::function<void()> task);
    void wait_for_completion();

private:
    std::vector<std::thread> workers;
    std::queue<std::function<void()>> tasks;
    std::mutex queue_mutex;
    std::condition_variable condition;
    bool stop;

    void worker_thread();
};


#endif //THREADPOOL_H
