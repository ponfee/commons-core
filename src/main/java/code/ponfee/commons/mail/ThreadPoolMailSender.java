package code.ponfee.commons.mail;

import static code.ponfee.commons.concurrent.ThreadPoolExecutors.INFINITY_QUEUE_EXECUTOR;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Sends emial by thread pool
 * 
 * @author fupf
 */
public class ThreadPoolMailSender {

    public static boolean send(MailSender mailSender, MailEnvelope envlop) {
        return send(mailSender, envlop, true);
    }

    public static boolean send(MailSender mailSender, MailEnvelope envlop, boolean async) {
        return send(mailSender, Collections.singletonList(envlop), async);
    }

    public static boolean send(MailSender mailSender, List<MailEnvelope> envlops) {
        return send(mailSender, envlops, true);
    }

    /**
     * 批量发送
     * @param mailSender  发送器
     * @param envlops     邮件内容集合
     * @param async       true异步；false同步；
     * @return
     */
    public static boolean send(MailSender mailSender, List<MailEnvelope> envlops, boolean async) {
        if (async) { // 异步发送
            envlops.forEach(
                e -> INFINITY_QUEUE_EXECUTOR.submit(
                    () -> mailSender.send(e)
                )
            );
            return true;
        } else { // 同步发送
            List<CompletableFuture<Boolean>> list = envlops.stream().map(
                e -> CompletableFuture.supplyAsync(
                   () -> mailSender.send(e), INFINITY_QUEUE_EXECUTOR
               )
            ).collect(Collectors.toList());

            return list.stream()
                       .map(CompletableFuture::join)
                       .reduce(Boolean::logicalAnd)
                       .orElse(false);
        }
    }

}
