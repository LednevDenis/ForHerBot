package com.example.SpringBot.service;


import com.example.SpringBot.config.BotConfig;
import com.example.SpringBot.model.LoveMessage;
import com.example.SpringBot.model.LoveMessageRepository;
import com.example.SpringBot.model.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vdurmont.emoji.EmojiParser;
import com.example.SpringBot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.*;
import org.telegram.telegrambots.meta.api.objects.commands.scope.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vdurmont.emoji.EmojiParser;

import java.io.File;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.*;
import org.telegram.telegrambots.meta.api.objects.commands.scope.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private BotConfig config;
    static final String HELP_TEXT = "Этот бот мой цифоровой подарок тебе. Каждый вечер, работая над ним, я думал о тебе";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoveMessageRepository loveRepository;

    static final int MAX_LOVE_ID_MINUS_ONE = 46;
    static final String NEXT_JOKE = "NEXT_JOKE";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/love", "get a random joke"));
        listOfCommands.add(new BotCommand("/help", "info how to use this bot"));
        listOfCommands.add(new BotCommand("/settings", "set your preferences"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public String getBotUsername() {
        return "NotesForKatyaBot";
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {


        if (update.hasMessage() && update.getMessage().hasText()) {

            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {

                case "/start" -> {
                    registerUsrer(update.getMessage());
                    showStart(chatId, update.getMessage().getChat().getFirstName());
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        TypeFactory typeFactory = objectMapper.getTypeFactory();
                        List<LoveMessage> loveList = objectMapper.readValue(new File("db/stupidstuff.json"),
                                typeFactory.constructCollectionType(List.class, LoveMessage.class));
                        loveRepository.saveAll(loveList);
                    } catch (Exception e) {
                        log.error(Arrays.toString(e.getStackTrace()));
                    }
                }

                case "/love" -> {

                    var love = getRandomLove();

                    love.ifPresent(randomJoke -> addButtonAndSendMessage(randomJoke.getBody(), chatId));


                }
                default -> commandNotFound(chatId);

            }

        }

        else if(update.hasCallbackQuery()){

            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if(callbackData.equals(NEXT_JOKE)) {

                var love = getRandomLove();

                love.ifPresent(randomJoke -> addButtonAndSendMessage(randomJoke.getBody(), chatId));

                //love.ifPresent(randomJoke -> addButtonAndEditText(randomJoke.getBody(), chatId, update.getCallbackQuery().getMessage().getMessageId()));

            }

        }
    }

    private Optional<LoveMessage> getRandomLove(){
        var r = new Random();
        var randomId = r.nextInt(MAX_LOVE_ID_MINUS_ONE) + 1;

//        return jokeRepository.findById(randomId);
        return loveRepository.findById(Long.valueOf(randomId));
    }

    private void addButtonAndSendMessage(String joke, long chatId){

        SendMessage message = new SendMessage();
        message.setText(joke);
        message.setChatId(chatId);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var inlinekeyboardButton = new InlineKeyboardButton();
        inlinekeyboardButton.setCallbackData(NEXT_JOKE);
        inlinekeyboardButton.setText(EmojiParser.parseToUnicode("Хочу еще записочку"));
        rowInline.add(inlinekeyboardButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        send(message);

    }

    private void addButtonAndEditText(String joke, long chatId, Integer messageId){

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(joke);
        message.setMessageId(messageId);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var inlinekeyboardButton = new InlineKeyboardButton();
        inlinekeyboardButton.setCallbackData(NEXT_JOKE);
        inlinekeyboardButton.setText(EmojiParser.parseToUnicode("Хочу еще записочку"));
        rowInline.add(inlinekeyboardButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        sendEditMessageText(message);
    }


    private void showStart(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode(
                "Здравствуй, " + name + ". Каждый раз когда тебе будет грустно и одиноко, приходи сюда и читай мои записочки. Выбери команду /love");
        sendMessage(answer, chatId);
    }

    private void commandNotFound(long chatId) {

        String answer = EmojiParser.parseToUnicode(
                "Command not recognized, please verify and try again :stuck_out_tongue_winking_eye: ");
        sendMessage(answer, chatId);

    }

    private void sendMessage(String textToSend, long chatId) {
        SendMessage message = new SendMessage(); // Create a message object object
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        send(message);
    }

    private void send(SendMessage msg) {
        try {
            execute(msg); // Sending our message object to user
        } catch (TelegramApiException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void sendEditMessageText(EditMessageText msg) {
        try {
            execute(msg); // Sending our message object to user
        } catch (TelegramApiException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void registerUsrer(Message msg) {
        if(userRepository.findById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setPhoneNumber(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }


}
