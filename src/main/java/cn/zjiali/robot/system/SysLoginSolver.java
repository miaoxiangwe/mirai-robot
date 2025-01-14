package cn.zjiali.robot.system;

import cn.hutool.core.codec.Base64;
import cn.zjiali.robot.annotation.Service;
import cn.zjiali.robot.constant.ServerUrl;
import cn.zjiali.robot.util.HttpUtil;
import cn.zjiali.robot.util.JsonUtil;
import cn.zjiali.robot.util.ObjectUtil;
import com.google.gson.JsonObject;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.utils.LoginSolver;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Scanner;


/**
 * 登录解决器
 *
 * @author zJiaLi
 * @since 2021-04-17 15:06
 */
@Service
public class SysLoginSolver extends LoginSolver {

    private static final MiraiLogger miraiLogger = MiraiLogger.create(SysLoginSolver.class.getName());

    @Nullable
    @Override
    public Object onSolvePicCaptcha(@NotNull Bot bot, byte[] bytes, @NotNull Continuation<? super String> continuation) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("qq", String.valueOf(bot.getId()));
        jsonObject.addProperty("base64", Base64.encode(bytes));
        String responseJson = HttpUtil.httpPost(ServerUrl.VERIFY_CODE_VIEW_URL, jsonObject);
        JsonObject response = JsonUtil.json2obj(responseJson, JsonObject.class);
        String verifyCodeUrl = response.get("data").getAsString();
        miraiLogger.warning("请打开以下网址后,在控制台输入验证码!");
        miraiLogger.warning(verifyCodeUrl);
        for (int i = 0; i < 3; i++) {
            String code = new Scanner(System.in).nextLine();
            if (ObjectUtil.isNullOrEmpty(code)) continue;
            return code;
        }
        return null;
    }

    @Nullable
    @Override
    public Object onSolveSliderCaptcha(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
        miraiLogger.warning("请打开以下网址后,完成滑动验证码验证!");
        miraiLogger.warning("完成后在控制台输入任意字符");
        miraiLogger.warning(s);
        return userVerify();
    }

    @Nullable
    @Override
    public Object onSolveUnsafeDeviceLoginVerify(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
        miraiLogger.warning("需要进行账户安全认证");
        miraiLogger.warning("该账户有[设备锁]/[不常用登录地点]/[不常用设备登录]的问题");
        miraiLogger.warning("请将该链接在浏览器中打开并完成认证, 成功后在控制台输入任意字符");
        miraiLogger.warning(s);
        return userVerify();
    }

    private String userVerify() {
        String code = null;
        int num = 0;
        do {
            try {
                num++;
                code = new Scanner(System.in).nextLine();
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (ObjectUtil.isNullOrEmpty(code) && num <= 5);
        return code;
    }
}
