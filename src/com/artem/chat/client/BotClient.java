package com.artem.chat.client;



import com.artem.chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client {
    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {

        return "date_bot_" + (int) (Math.random() * 100);
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(":")) {
                String[] split = message.trim().split(":");
                String name = split[0].trim();
                String text = split[1].trim();
                String pattern = "";
                switch (text) {
                    case "дата":
                        pattern = "d.MM.YYYY";
                        break;

                    case "день":
                        pattern = "d";
                        break;
                    case "месяц":
                        pattern = "MMMM";
                        break;
                    case "год":
                        pattern = "YYYY";
                        break;
                    case "время":
                        pattern = "H:mm:ss";
                        break;
                    case "час":
                        pattern = "H";
                        break;
                    case "минуты":
                        pattern = "m";
                        break;
                    case "секунды":
                        pattern = "s";
                        break;
                    default:
                        pattern = "";
                }
                if (!pattern.isEmpty()) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    GregorianCalendar calendar = new GregorianCalendar();
                    Date time = calendar.getTime();
                    String ansverDate = simpleDateFormat.format(time);
                    String rezult = "Информация для " + name + ": " + ansverDate;
                    sendTextMessage(rezult);
                }

            }


        }
    }
    

    public static void main(String[] args) {
        BotClient client = new BotClient();
        client.run();

    }
}
