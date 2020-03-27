package com.mercury.platform.shared;

import com.mercury.platform.shared.entity.message.CurrencyTradeNotificationDescriptor;
import com.mercury.platform.shared.entity.message.ItemTradeNotificationDescriptor;
import com.mercury.platform.shared.entity.message.NotificationDescriptor;
import com.mercury.platform.shared.entity.message.NotificationType;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// DealdyCrush
import java.io.UnsupportedEncodingException;

// 가격없는 것 : 2019/06/03 16:24:54 160702546 a5f [INFO Client 7372] @From <AUSSIE> Pseudo_F: 안녕하세요, 챌린지(보관함 탭 "Gift #1", 위치: 왼쪽 15, 상단 3)에 올려놓은 거물 징 박힌 허리띠을(를) 구매하고 싶습니다

public class MessageParser {
    private final static String poeTradeStashTabPattern = "^(.*\\s)?(.+): (.+ to buy your\\s+?(.+?)(\\s+?listed for\\s+?([\\d\\.]+?)\\s+?(.+))?\\s+?in\\s+?(.+?)\\s+?\\(stash tab \"(.*)\"; position: left (\\d+), top (\\d+)\\)\\s*?(.*))$";
    private final static String poeTradePattern = "^(.*\\s)?(.+): (.+ to buy your\\s+?(.+?)(\\s+?listed for\\s+?([\\d\\.]+?)\\s+?(.+))?\\s+?in\\s+?(.*?))$";
    private final static String poeAppPattern = "^(.*\\s)?(.+): (\\s*?wtb\\s+?(.+?)(\\s+?listed for\\s+?([\\d\\.]+?)\\s+?(.+))?\\s+?in\\s+?(.+?)\\s+?\\(stash\\s+?\"(.*?)\";\\s+?left\\s+?(\\d+?),\\s+?top\\s+(\\d+?)\\)\\s*?(.*))$";
    private final static String poeAppBulkCurrenciesPattern = "^(.*\\s)?(.+): (\\s*?wtb\\s+?(.+?)(\\s+?listed for\\s+?([\\d\\.]+?)\\s+?(.+))?\\s+?in\\s+?(.+?)\\s+?\\(stash\\s+?\"(.*?)\";\\s+?left\\s+?(\\d+?),\\s+?top\\s+(\\d+?)\\)\\s*?(.*))$";
    private final static String poeCurrencyPattern = "^(.*\\s)?(.+): (.+ to buy your (\\d+(\\.\\d+)?)? (.+) for my (\\d+(\\.\\d+)?)? (.+) in (.*?)\\.\\s*(.*))$";

    /*
    @수신 Deadly_SoulBane: Hi, I would like to buy your Pandemonium Ward Steel Circlet listed for 12 chaos in Synthesis (stash tab "$ Wts $"; position: left 1, top 9) aa Offer aa
    ^(.*\s)?(.+): (.+ to buy your\s+?(.+?)(\s+?listed for\s+?([\d\.]+?)\s+?(.+))?\s+?in\s+?(.+?)\s+?\(stash tab "(.*)"; position: left (\d+), top (\d+)\)\s*?(.*))$
    @수신 Deadly_SoulBane: 안녕하세요, 챌린지(보관함 탭 "~price 20 chaos", 위치: 왼쪽 24, 상단 6)에 20 chaos(으)로 올려놓은 비애의 손톱 사파이어 반지을(를) 구매하고 싶습니다 aa Offer aa
    안녕하세요, 챌린지(보관함 탭 "~price 20 chaos", 위치: 왼쪽 24, 상단 6)에 20 chaos(으)로 올려놓은 비애의 손톱 사파이어 반지을(를) 구매하고 싶습니다 aa Offer aa
    RegExp. => ^(.*\s)?(.+):(.*?)보관함 탭(.*?)"(.*)", 위치: 왼쪽 (\d+), 상단 (\d+)\)에 (\d+) (.*?)\(으\)로 올려놓은(.?\s)(.+)을\(를\)
    (.*\s)?(.+):(.*?)보관함 탭(.*?)\"(.*)\", 위치: 왼쪽 (\d+), 상단 (\d+)\)에 (\d+) (.*?)\(으\)로 올려놓은(.?\s)(.+)을\(를\)
    ^(.*\s)?(.+): 안녕하세요, (\s*)?(.+)\(보관함 탭(.*?)\"(.*)\", 위치: 왼쪽 (\d+), 상단 (\d+)\)에 (\d+) (.*?)\(으\)로 올려놓은(.?\s)(.+)을\(를\) 구매하고 싶습니다.
    ^(.*\s)?(.+): 안녕하세요, (\s*)?(.+)\(보관함 탭(.*?)\"(.*)\", 위치: 왼쪽 (\d+), 상단 (\d+)\)에 (\d+) (.*?)\(으\)로 올려놓은(.?\s)(.+)을\(를\) 구매하고 싶습니다.\s*(.*)$
    Gorup0 : Original Msg All
    Group1 : @수신
    2 : Nick
    4 : League
    6 : Tab
    7 : Left
    8 : Top
    9 : Price
    10 : Which Currency
    12 : Item Name
    13 : Offer
    */
    // @From Deadly_SoulBane: 안녕하세요, 챌린지(보관함 탭 "$ Wts $", 위치: 왼쪽 7, 상단 2)에 2 chaos(으)로 올려놓은 레벨 1 20% 격노하는 혼백 소환을(를) 구매하고 싶습니다
    // private final static String poeKAKAOTabPattern = "^(.*\\s)?(.+): 안녕하세요, (\\s*)?(.+)\\(보관함 탭(.*?)\\\"(.*)\\\", 위치: 왼쪽 (\\d+), 상단 (\\d+)\\)에 (\\d+) (.*?)\\(으\\)로 올려놓은(.?\\s)(.+)을\\(를\\) 구매하고 싶습니다.\\s*(.*)$";
    private final static String poeKAKAOTabPattern = "^(.*\\s)?(.+): 안녕하세요, (\\s*)?(.+)\\(보관함 탭(.*?)\\\"(.*)\\\", 위치: 왼쪽 (\\d+), 상단 (\\d+)\\)에 (\\d+) (.*?)\\(으\\)로 올려놓은(.?\\s)(.+)을\\(를\\) 구매하고 싶습니다";
    private final static String poeKAKAOTabPatternOffer
            = "^(.*\\s)?(.+): 안녕하세요, (\\s*)?(.+)\\(보관함 탭(.*?)\\\"(.*)\\\", 위치: 왼쪽 (\\d+), 상단 (\\d+)\\)에 (\\d+) (.*?)\\(으\\)로 올려놓은(.?\\s)(.+)을\\(를\\) 구매하고 싶습니다.\\s*(.*)$";

    // 가격을 정해놓지 않는 것
    // @수신 Deadly_SoulBane: 안녕하세요, 챌린지(보관함 탭 "Gift #1", 위치: 왼쪽 15, 상단 3)에 올려놓은 거물 징 박힌 허리띠을(를) 구매하고 싶습니다
    // ^(.*\s)?(.+): 안녕하세요, (\s*)?(.+)\(보관함 탭(.*?)\"(.*)\", 위치: 왼쪽 (\d+), 상단 (\d+)\)에 올려놓은 (.*?\s*)을\(를\) 구매하고 싶습니다.
    // ^(.*\s)?(.+): 안녕하세요, (\s*)?(.+)\(보관함 탭(.*?)\"(.*)\", 위치: 왼쪽 (\d+), 상단 (\d+)\)에 올려놓은 (.*?\s*)을\(를\) 구매하고 싶습니다.\s*(.*)$
    private final static String poeKAKAOTabPatternNoPrice = "^(.*\\s)?(.+): 안녕하세요, (\\s*)?(.+)\\(보관함 탭(.*?)\\\"(.*)\\\", 위치: 왼쪽 (\\d+), 상단 (\\d+)\\)에 올려놓은 (.*?\\s*)을\\(를\\) 구매하고 싶습니다";
    private final static String poeKAKAOTabPatternNoPriceOffer = "^(.*\\s)?(.+): 안녕하세요, (\\s*)?(.+)\\(보관함 탭(.*?)\\\"(.*)\\\", 위치: 왼쪽 (\\d+), 상단 (\\d+)\\)에 올려놓은 (.*?\\s*)을\\(를\\) 구매하고 싶습니다\\s*(.*)$";

    private Pattern poeAppItemPattern;
    private Pattern poeTradeStashItemPattern;
    private Pattern poeTradeItemPattern;
    private Pattern poeTradeCurrencyPattern;

    private Pattern poeTradeStashKAKAOPattern;
    private Pattern poeTradeStashKAKAOOfferPattern;
    private Pattern poeTradeStashKAKAONoPricePattern;
    private Pattern poeTradeStashKAKAONoPriceOfferPattern;

    public MessageParser() {
        this.poeAppItemPattern = Pattern.compile(poeAppPattern);
        this.poeTradeStashItemPattern = Pattern.compile(poeTradeStashTabPattern);
        this.poeTradeItemPattern = Pattern.compile(poeTradePattern);
        this.poeTradeCurrencyPattern = Pattern.compile(poeCurrencyPattern);

        this.poeTradeStashKAKAOPattern = Pattern.compile(poeKAKAOTabPattern);
        this.poeTradeStashKAKAOOfferPattern = Pattern.compile(poeKAKAOTabPatternOffer);
        this.poeTradeStashKAKAONoPricePattern = Pattern.compile(poeKAKAOTabPatternNoPrice);
        this.poeTradeStashKAKAONoPriceOfferPattern = Pattern.compile(poeKAKAOTabPatternNoPriceOffer);
    }

    public NotificationDescriptor parse(String fullMessage) {

        Matcher poeAppItemMatcher = poeAppItemPattern.matcher(fullMessage);
        if (poeAppItemMatcher.find()) {
            if(poeAppItemMatcher.groupCount()>3) { // Modified by DEADLYCRUSH
                ItemTradeNotificationDescriptor tradeNotification = new ItemTradeNotificationDescriptor();
                tradeNotification.setWhisperNickname(poeAppItemMatcher.group(2));
                tradeNotification.setSourceString(poeAppItemMatcher.group(3));
                tradeNotification.setItemName(poeAppItemMatcher.group(4));
                if (poeAppItemMatcher.group(5) != null) {
                    tradeNotification.setCurCount(Double.parseDouble(poeAppItemMatcher.group(6)));
                    tradeNotification.setCurrency(poeAppItemMatcher.group(7));
                } else {
                    tradeNotification.setCurCount(0d);
                    tradeNotification.setCurrency("???");
                }
                tradeNotification.setLeague(poeAppItemMatcher.group(8));
                if (poeAppItemMatcher.group(9) != null) {
                    tradeNotification.setTabName(poeAppItemMatcher.group(9));
                    tradeNotification.setLeft(Integer.parseInt(poeAppItemMatcher.group(10)));
                    tradeNotification.setTop(Integer.parseInt(poeAppItemMatcher.group(11)));
                }
                tradeNotification.setOffer(poeAppItemMatcher.group(12));
                tradeNotification.setType(NotificationType.INC_ITEM_MESSAGE);
                return tradeNotification;
            }
        }

        Matcher poeTradeStashItemMatcher = poeTradeStashItemPattern.matcher(fullMessage);
        if (poeTradeStashItemMatcher.find()) {
            if(poeTradeStashItemMatcher.groupCount()>3) { // Modified by DEADLYCRUSH
                ItemTradeNotificationDescriptor tradeNotification = new ItemTradeNotificationDescriptor();
                tradeNotification.setWhisperNickname(poeTradeStashItemMatcher.group(2));
                tradeNotification.setSourceString(poeTradeStashItemMatcher.group(3));
                tradeNotification.setItemName(poeTradeStashItemMatcher.group(4));
                if (poeTradeStashItemMatcher.group(6) != null) {
                    tradeNotification.setCurCount(Double.parseDouble(poeTradeStashItemMatcher.group(6)));
                    tradeNotification.setCurrency(poeTradeStashItemMatcher.group(7));
                } else {
                    tradeNotification.setCurCount(0d);
                    tradeNotification.setCurrency("???");
                }
                tradeNotification.setLeague(poeTradeStashItemMatcher.group(8));
                tradeNotification.setTabName(poeTradeStashItemMatcher.group(9));
                tradeNotification.setLeft(Integer.parseInt(poeTradeStashItemMatcher.group(10)));
                tradeNotification.setTop(Integer.parseInt(poeTradeStashItemMatcher.group(11)));
                if (poeTradeStashItemMatcher.groupCount() == 12) // Modified by DEADLYCRUSH
                    tradeNotification.setOffer(poeTradeStashItemMatcher.group(12));
                tradeNotification.setType(NotificationType.INC_ITEM_MESSAGE);
                return tradeNotification;
            }
        }

        Matcher poeTradeCurrencyMatcher = poeTradeCurrencyPattern.matcher(fullMessage);
        if (poeTradeCurrencyMatcher.find()) {
            if(poeTradeCurrencyMatcher.groupCount()>3) { // Modified by DEADLYCRUSH
                CurrencyTradeNotificationDescriptor tradeNotification = new CurrencyTradeNotificationDescriptor();

                if (poeTradeCurrencyMatcher.group(6).contains("&") || poeTradeCurrencyMatcher.group(6).contains(",")) {  //todo this shit for bulk map
                    String bulkItems = poeTradeCurrencyMatcher.group(4) + " " + poeTradeCurrencyMatcher.group(6);
                    tradeNotification.setItems(Arrays.stream(StringUtils.split(bulkItems, ",&")).map(String::trim).collect(Collectors.toList()));
                } else {
                    tradeNotification.setCurrForSaleCount(Double.parseDouble(poeTradeCurrencyMatcher.group(4)));
                    tradeNotification.setCurrForSaleTitle(poeTradeCurrencyMatcher.group(6));
                }

                tradeNotification.setWhisperNickname(poeTradeCurrencyMatcher.group(2));
                tradeNotification.setSourceString(poeTradeCurrencyMatcher.group(3));
                tradeNotification.setCurCount(Double.parseDouble(poeTradeCurrencyMatcher.group(7)));
                tradeNotification.setCurrency(poeTradeCurrencyMatcher.group(9));
                tradeNotification.setLeague(poeTradeCurrencyMatcher.group(10));
                tradeNotification.setOffer(poeTradeCurrencyMatcher.group(11));
                tradeNotification.setType(NotificationType.INC_CURRENCY_MESSAGE);
                return tradeNotification;
            }
        }

        Matcher poeTradeItemMatcher = poeTradeItemPattern.matcher(fullMessage);
        if (poeTradeItemMatcher.find()) {
            if(poeTradeItemMatcher.groupCount()>3) { // Modified by DEADLYCRUSH
                ItemTradeNotificationDescriptor tradeNotification = new ItemTradeNotificationDescriptor();
                tradeNotification.setWhisperNickname(poeTradeItemMatcher.group(2));
                tradeNotification.setSourceString(poeTradeItemMatcher.group(3));
                tradeNotification.setItemName(poeTradeItemMatcher.group(4));
                if (poeTradeItemMatcher.group(5) != null) {
                    tradeNotification.setCurCount(Double.parseDouble(poeTradeItemMatcher.group(6)));
                    tradeNotification.setCurrency(poeTradeItemMatcher.group(7));
                } else {
                    tradeNotification.setCurCount(0d);
                    tradeNotification.setCurrency("???");
                }
                tradeNotification.setLeague(poeTradeItemMatcher.group(8));
                tradeNotification.setType(NotificationType.INC_ITEM_MESSAGE);
                return tradeNotification;
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////
        // for KAKAO POE Client
        //////////////////////////////////////////////////////////////////////////////////////////////////////

        // this.poeTradeStashKAKAOPattern = Pattern.compile(poeKAKAOTabPattern);
        // @From Deadly_SoulBane: Hi, I would like to buy your Pandemonium Ward Steel Circlet listed for 12 chaos in Synthesis (stash tab "$ Wts $"; position: left 1, top 9)
        // @Deadly_UltimateAura Hi, I would like to buy your Panicked Eternal Life Flask of Grounding listed for 1 chaos in Synthesis (stash tab "Flask #1"; position: left 12, top 1)
        // @Deadly_SoulBane 안녕하세요, 챌린지(보관함 탭 "Flask #1", 위치: 왼쪽 1, 상단 7)에 2 chaos(으)로 올려놓은 영속적인 토파즈 플라스크 - 저항을(를) 구매하고 싶습니다
        // @수신 Deadly_SoulBane: 안녕하세요, 챌린지(보관함 탭 "~price 20 chaos", 위치: 왼쪽 24, 상단 6)에 20 chaos(으)로 올려놓은 비애의 손톱 사파이어 반지을(를) 구매하고 싶습니다
        // @수신 Deadly_SoulBane: 안녕하세요, 챌린지(보관함 탭 "Gift #1", 위치: 왼쪽 15, 상단 3)에 올려놓은 거물 징 박힌 허리띠을(를) 구매하고 싶습니다
        /*
        Gorup0 : Original Msg All
        Group1 : @수신
        2 : Nick
        4 : League
        6 : Tab
        7 : Left
        8 : Top
        9 : Price
        10 : Which Currency
        12 : Item Name
        13 : Offer
        */

        // Price KAKAO with Offer
        Matcher poeTradeStashKAKAOItemOfferMatcher = poeTradeStashKAKAOOfferPattern.matcher(fullMessage);
        if (poeTradeStashKAKAOItemOfferMatcher.find() ) {
            if(poeTradeStashKAKAOItemOfferMatcher.groupCount()==13) {
                ItemTradeNotificationDescriptor tradeNotification = new ItemTradeNotificationDescriptor();
                tradeNotification.setWhisperNickname(poeTradeStashKAKAOItemOfferMatcher.group(2)); // Nick
                tradeNotification.setSourceString(poeTradeStashKAKAOItemOfferMatcher.group(0)); // Source
                tradeNotification.setItemName(poeTradeStashKAKAOItemOfferMatcher.group(12)); // Item Name
                if (poeTradeStashKAKAOItemOfferMatcher.group(9) != null) { // Price
                    tradeNotification.setCurCount(Double.parseDouble(poeTradeStashKAKAOItemOfferMatcher.group(9)));
                    tradeNotification.setCurrency(poeTradeStashKAKAOItemOfferMatcher.group(10)); // Which Currency
                } else {
                    tradeNotification.setCurCount(0d);
                    tradeNotification.setCurrency("???");
                }
                tradeNotification.setLeague(poeTradeStashKAKAOItemOfferMatcher.group(4)); // League
                tradeNotification.setTabName(poeTradeStashKAKAOItemOfferMatcher.group(6)); // Tab
                tradeNotification.setLeft(Integer.parseInt(poeTradeStashKAKAOItemOfferMatcher.group(7))); // Left
                tradeNotification.setTop(Integer.parseInt(poeTradeStashKAKAOItemOfferMatcher.group(8))); // Top
                tradeNotification.setOffer(poeTradeStashKAKAOItemOfferMatcher.group(13)); // Offer
                tradeNotification.setType(NotificationType.INC_ITEM_MESSAGE);
                return tradeNotification;
            }
        }

        // Price KAKAO
        Matcher poeTradeStashKAKAOItemMatcher = poeTradeStashKAKAOPattern.matcher(fullMessage);
        if (poeTradeStashKAKAOItemMatcher.find() ) {
            if(poeTradeStashKAKAOItemMatcher.groupCount()==12) {
                ItemTradeNotificationDescriptor tradeNotification = new ItemTradeNotificationDescriptor();
                tradeNotification.setWhisperNickname(poeTradeStashKAKAOItemMatcher.group(2)); // Nick
                tradeNotification.setSourceString(poeTradeStashKAKAOItemMatcher.group(0)); // Source
                tradeNotification.setItemName(poeTradeStashKAKAOItemMatcher.group(12)); // Item Name
                if (poeTradeStashKAKAOItemMatcher.group(9) != null) { // Price
                    tradeNotification.setCurCount(Double.parseDouble(poeTradeStashKAKAOItemMatcher.group(9)));
                    tradeNotification.setCurrency(poeTradeStashKAKAOItemMatcher.group(10)); // Which Currency
                } else {
                    tradeNotification.setCurCount(0d);
                    tradeNotification.setCurrency("???");
                }
                tradeNotification.setLeague(poeTradeStashKAKAOItemMatcher.group(4)); // League
                tradeNotification.setTabName(poeTradeStashKAKAOItemMatcher.group(6)); // Tab
                tradeNotification.setLeft(Integer.parseInt(poeTradeStashKAKAOItemMatcher.group(7))); // Left
                tradeNotification.setTop(Integer.parseInt(poeTradeStashKAKAOItemMatcher.group(8))); // Top
                tradeNotification.setType(NotificationType.INC_ITEM_MESSAGE);
                return tradeNotification;
            }
        }

        // No Price KAKAO with Offer
        Matcher poeTradeStashKAKAOItemNoPriceOfferMatcher = poeTradeStashKAKAONoPriceOfferPattern.matcher(fullMessage);
        if (poeTradeStashKAKAOItemNoPriceOfferMatcher.find() ) {
            //if(poeTradeStashKAKAOItemNoPriceOfferMatcher.groupCount()==10) {
                ItemTradeNotificationDescriptor tradeNotification = new ItemTradeNotificationDescriptor();
                tradeNotification.setWhisperNickname(poeTradeStashKAKAOItemNoPriceOfferMatcher.group(2)); // Nick
                tradeNotification.setSourceString(poeTradeStashKAKAOItemNoPriceOfferMatcher.group(0)); // Source
                tradeNotification.setItemName(poeTradeStashKAKAOItemNoPriceOfferMatcher.group(9)); // Item Name
                tradeNotification.setCurCount(0d);
                tradeNotification.setCurrency("???");
                tradeNotification.setLeague(poeTradeStashKAKAOItemNoPriceOfferMatcher.group(4)); // League
                tradeNotification.setTabName(poeTradeStashKAKAOItemNoPriceOfferMatcher.group(6)); // Tab
                tradeNotification.setLeft(Integer.parseInt(poeTradeStashKAKAOItemNoPriceOfferMatcher.group(7))); // Left
                tradeNotification.setTop(Integer.parseInt(poeTradeStashKAKAOItemNoPriceOfferMatcher.group(8))); // Top
                if(poeTradeStashKAKAOItemNoPriceOfferMatcher.group(10)!=null)
                    tradeNotification.setOffer(poeTradeStashKAKAOItemNoPriceOfferMatcher.group(10)); // Offer
                tradeNotification.setType(NotificationType.INC_ITEM_MESSAGE);
                return tradeNotification;
            //}
        }

        // No Price KAKAO
        /*Matcher poeTradeStashKAKAOItemNoPriceMatcher = poeTradeStashKAKAONoPricePattern.matcher(fullMessage);
        if (poeTradeStashKAKAOItemNoPriceMatcher.find() ) {
            if(poeTradeStashKAKAOItemNoPriceMatcher.groupCount()==9) {
                ItemTradeNotificationDescriptor tradeNotification = new ItemTradeNotificationDescriptor();
                tradeNotification.setWhisperNickname(poeTradeStashKAKAOItemNoPriceMatcher.group(2)); // Nick
                tradeNotification.setSourceString(poeTradeStashKAKAOItemNoPriceMatcher.group(0)); // Source
                tradeNotification.setItemName(poeTradeStashKAKAOItemNoPriceMatcher.group(9)); // Item Name
                //tradeNotification.setCurCount(0d);
                //tradeNotification.setCurrency("???");
                tradeNotification.setLeague(poeTradeStashKAKAOItemNoPriceMatcher.group(4)); // League
                tradeNotification.setTabName(poeTradeStashKAKAOItemNoPriceMatcher.group(6)); // Tab
                tradeNotification.setLeft(Integer.parseInt(poeTradeStashKAKAOItemNoPriceMatcher.group(7))); // Left
                tradeNotification.setTop(Integer.parseInt(poeTradeStashKAKAOItemNoPriceMatcher.group(8))); // Top
                tradeNotification.setType(NotificationType.INC_ITEM_MESSAGE);
                return tradeNotification;
            }
        }*/



        return null;
    }
}
