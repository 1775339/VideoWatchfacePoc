package com.titan.titanvideotrimmingpoc

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        initLogger()
    }

//    private fun initLogger() {
//        val formatStrategy: FormatStrategy = CsvFormatStrategy.newBuilder()
//                .logStrategy(LogcatLogStrategy())
//                .build()
//        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
//        val diskPath = externalMediaDirs[0].absolutePath
//        val folder = diskPath + File.separator + "log"
//        val ht = HandlerThread("AndroidFileLogger.$folder")
//        ht.start()
//        val logHandler = WriteLogHandler(ht.looper, folder)
//        val logStrategy = DiskLogStrategy(logHandler)
//        val diskFormatStrategy = CsvFormatStrategy.newBuilder()
//                .logStrategy(logStrategy).build()
//        Logger.addLogAdapter(object : DiskLogAdapter(diskFormatStrategy) {
//            override fun isLoggable(priority: Int, tag: String?): Boolean {
//                return true
//            }
//        })
//        Timber.plant(object : Timber.Tree() {
//            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//                Logger.log(priority, tag, message, t)
//            }
//        })
//        Timber.d("isDebug:%s", BuildConfig.DEBUG)
//        WriteLogUtils.init()
//    }

//    internal class WriteLogHandler(looper: Looper, private val folder: String) : Handler(looper) {
//        override fun handleMessage(msg: Message) {
//            val content = msg.obj as String
//            val logFile = getLogFile(folder)
//            FileIOUtils.writeFileFromString(logFile, content, true)
//        }
//
//        private fun getLogFile(folderName: String): File {
//            val folder = File(folderName)
//            if (!folder.exists()) {
//                //TODO: What if folder is not created, what happens then?
//                folder.mkdirs()
//            }
//            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            return File(folder, String.format("%s.txt", sdf.format(Date())))
//        }
//    }

}