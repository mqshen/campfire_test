package org.goldratio.xmpp.scheduler

import java.util.concurrent.{Future, TimeUnit, Executors, ConcurrentHashMap}

/**
 * Created by GoldRatio on 4/16/14.
 */
class CancelableScheduler(threadPoolSize: Int) {
  val scheduledFutures = new ConcurrentHashMap[String, Future[_]]()

  val executorService = Executors.newScheduledThreadPool(threadPoolSize)

  def  cancel(key: String) {
    val future = scheduledFutures.remove(key)
    if (future != null) {
      future.cancel(false)
    }
  }

  def schedule(runnable: Runnable , delay: Long, unit: TimeUnit ) {
    executorService.schedule(runnable, delay, unit)
  }

  def schedule(key: String, runnable: Runnable , delay: Long, unit: TimeUnit ) {
    val future = executorService.schedule(new Runnable() {
      def run() {
        try {
          runnable.run()
        } finally {
          scheduledFutures.remove(key)
        }
      }
    }, delay, unit)
    scheduledFutures.put(key, future)
  }

  def shutdown() {
    executorService.shutdownNow();
    try {
      executorService.awaitTermination(10, TimeUnit.SECONDS);
    }
    catch {
      case e: InterruptedException =>
        e.printStackTrace()
    }
  }






}
