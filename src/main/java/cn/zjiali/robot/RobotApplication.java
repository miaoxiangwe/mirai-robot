package cn.zjiali.robot;

import cn.zjiali.robot.annotation.Application;
import cn.zjiali.robot.config.AppConfig;
import cn.zjiali.robot.factory.ServiceFactory;
import cn.zjiali.robot.handler.MessageHandler;
import cn.zjiali.robot.main.ApplicationBootStrap;
import cn.zjiali.robot.system.SysLoginSolver;
import cn.zjiali.robot.util.DeviceUtil;
import cn.zjiali.robot.util.ObjectUtil;
import kotlin.Unit;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.*;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.utils.*;


/**
 * @author zJiaLi
 * @since 2020-10-29 11:09
 */
@Application(basePackages = {"cn.zjiali.robot"})
public class RobotApplication {

    private static final MiraiLogger miraiLogger = new PlatformLogger(RobotApplication.class.getName());

    private static void init() {
        miraiLogger.info("====初始化配置中====");
        try {
            ApplicationBootStrap.getInstance().init();
            miraiLogger.info("====初始化配置完成====");
            miraiLogger.info("⭐⭐⭐⭐⭐⭐GitHub: https://github.com/SLiGe/mirai-robot ⭐⭐⭐⭐⭐⭐");
        } catch (Exception e) {
            miraiLogger.error("====初始化配置出错,e: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        init();
        long qq = Long.parseLong(AppConfig.applicationConfig.getQq());
        String password = AppConfig.applicationConfig.getPassword();
        Bot bot = BotFactory.INSTANCE.newBot(qq, password, new BotConfiguration() {
            {
                //加载设备信息
                loadDeviceInfoJson(DeviceUtil.getDeviceInfoJson(AppConfig.applicationConfig.getQq()));
                //设置登录解决器
                setLoginSolver((LoginSolver) ServiceFactory.get(SysLoginSolver.class.getSimpleName()));
                // 选择协议
                setProtocol(switchProtocol());
            }
        });

        bot.login();
        EventChannel<BotEvent> eventChannel = bot.getEventChannel();
        // 创建监听
        eventChannel.exceptionHandler(e -> {
            e.printStackTrace();
            return Unit.INSTANCE;
        });
        eventChannel.subscribeAlways(GroupMessageEvent.class, MessageHandler::handleGroupMessage);
        eventChannel.subscribeAlways(FriendMessageEvent.class, MessageHandler::handleFriendMessage);
        eventChannel.subscribeAlways(NewFriendRequestEvent.class, NewFriendRequestEvent::accept);
        bot.join(); // 阻塞当前线程直到 bot 离线
    }

    /**
     * 选择协议
     *
     * @return 协议
     */
    private static BotConfiguration.MiraiProtocol switchProtocol() {
        String robotProtocol = System.getProperty("robot.protocol");
        if (!ObjectUtil.isNullOrEmpty(robotProtocol)) {
            switch (robotProtocol) {
                case "0":
                    return (BotConfiguration.MiraiProtocol.ANDROID_PHONE);
                case "1":
                    return (BotConfiguration.MiraiProtocol.ANDROID_PAD);
                case "2":
                    return (BotConfiguration.MiraiProtocol.ANDROID_WATCH);
            }
        }
        return (BotConfiguration.MiraiProtocol.ANDROID_PHONE);
    }

}
