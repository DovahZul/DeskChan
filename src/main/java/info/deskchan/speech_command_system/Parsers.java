package info.deskchan.speech_command_system;

import info.deskchan.core_utils.LimitHashMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parsers {
    public static void Testing(){
        String tests1[] = new String[]{ "две тыщи", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять", "седьмое", "второе", "две тысячи шестого", "девятое прошлого", "пятьюест" , "триста миллионов семсот сорак пят тысяч двести тридать один двадцать четыре тысячи три миллиарда", "пятьбдесят", "птдесят", "пятдесчт", "пяддесят пят", "пяццот", "пятсот", "двадцат второй", "добрый день господа", "мне 70 лет", "20 тысяч 543", "тысяча 200 сорок три", "пятае", "сенадцать",
                "тринадцать восемьдесят девять", "восемь восемьсот пять пять пять три пять три пять", "восемь восемьсот пробел пять пять пять три пять три пять", "девятьсот шестнадцать", "восемь девятьсот шестнадцать"};
        for(int i=0;i<tests1.length;i++) {
            ArrayList<String> words = PhraseComparison.toRuleWords(tests1[i]);
            boolean[] ar = new boolean[words.size()];
            System.out.println("\""+tests1[i] + "\" / " + parseInteger(words, ar));
            //for(int k=0; k<words.size(); k++)
            //    System.out.print(ar[k]+" ");
            //System.out.println("\n");
        }
        String tests2[] = new String[]{ "сейчас", "сегодня в 12 30", "сегодня", "завтра", "послезавтра в 5 часов", "5 часов вечера 30 минут 29 июля", "за 50 минут до сегодня", "за три часа до завтрашнего дня", "7 марта седьмое", "2012 марта седьмое", "через месяц", "в следующий понедельник", "в следующее вторник", "вторник", "прошлый вторник", "среда", "следующая среда", "пятое июня", "после четверга" , "за 5 дней до четверга", "за 12 дней до пятого сентября", "за 2 дна до августа две тысячи шестого", "Пятое мая", "Седьмое июня следующего года",
                "три утра", "девять вечера", "Просто седьмое июня", "через трое суток", "через четверть часа", "полторы недели назад", "30 февраля", "29 февраля прошлого года", "Второе число следующего месяца", "Девятое прошлого года следующего месяца", "шестой понедельник ноября", "Этот день в прошлом году", "Это число в следующем году", "через пятницу", "пятницу назад", "3 субботы назад", "через воскресенье", "Третья суббота июня", "послезавтра", "позапозавчера", "17 декабря этого года",
                "за неделю до", "после послепосле послезавтра","15 02 2017", "2 15 2017", "15/02-2017 23 34 05 23", "15/02-2017 23:34:05 23", "15/02-2017 23/34/05 23", "23:34:05.23 15.02.2017", "23:30 15/08", "15 августа 23:30", "через 5 минут", "послезавтра в 20:18"};
        for(int i=0;i<tests2.length;i++) {
            ArrayList<String> words = PhraseComparison.toClearWords(tests2[i]);
            Calendar cal = Calendar.getInstance();
            boolean[] ar = new boolean[words.size()];
            cal.setTimeInMillis(parseDate(words, ar));
            System.out.println("\""+tests2[i] + "\" / " + cal.getTime().toString());
            //for(int k=0; k<words.size(); k++)
            //    System.out.print(ar[k]+" ");
            //System.out.println("\n");
        }
    }

    private static long DAY = 86400000L;
    private static class Pair<K, V>{
        K one; V two;
        Pair(K key, V value){
            one = key; two = value;
        }
    }
    private static class DateTimeStruct{
        static final DateTimeStruct Zero = new DateTimeStruct(null, null);

        Long date = null;
        Long time = null;

        DateTimeStruct(Long d, Long t){ date = d; time = t; }
        DateTimeStruct(Calendar calendar){
            date = calendar.getTimeInMillis();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            time = date - calendar.getTimeInMillis();
            date = calendar.getTimeInMillis();
        }

        boolean equals(DateTimeStruct other){
            return ((date==null && other.date==null ||
                    other.date!=null && date.equals(other.date)) &&
                    (time==null && other.time==null ||
                            time!=null && time.equals(other.time)));
        }

        long getTime(){ return (date!=null ? date : 0)+(time!=null ? time : 0); }

        static DateTimeStruct fromTime(Calendar calendar){
            Long date = calendar.getTimeInMillis();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return new DateTimeStruct(null, date-calendar.getTimeInMillis());
        }

        static DateTimeStruct fromDate(Calendar calendar){
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return new DateTimeStruct(calendar.getTimeInMillis(), null);
        }
    }
    private static class WordFinder{
        private static final ArrayList< Pair<String, Double> > floats = getPairList(
                new String[]{ "половина", "полтора", "четверть", "треть"},
                new Double[]{ 0.5, 1.5, 0.25, 0.33 }
        );
        private static final String[] tenDigit = {
                "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать"
        };
        private static final String[] dozens = {
                "десять", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"
        };
        private static final String[][] hundreds = {
                { "сто", "cотня" }, { "двести", "двухсотый" }, { "тристa", "трехсотый" }, { "четыреста", "четырехcoтый" }, { "пятьсот", "пятисотый" }, { "шестьсот", "шестисотый" }, { "семьсот", "семисотый" }, { "восемьсот", "восьмисотый" }, { "девятьсот", "девятисотый" }
        };
        private static final String[][] digits = {
                {"ноль", "нулевой"},
                {"один", "первый"},
                {"два", "пара", "двойка", "второй"},
                {"три", "тройка", "третий", "трёх"},
                {"четыре", "четвёрка", "четвёртый", "четырёх"},
                {"пять", "пятак", "пятёрка", "пятый"},
                {"шесть", "шестёрка", "шестой", "шестых"},
                {"семь", "семёрка", "седьмой"},
                {"восемь", "восьмёрка", "восьмой"},
                {"девять", "девятка", "девятый"}
        };
        private static final ArrayList< Pair<String, Integer> > thousands = getPairList(
                new String[] { "единиц", "десяток", "сотня", "тысяча", "тыща", "лям", "миллион", "миллиард", "триллион"   },
                new Integer[]{ 0 ,       1 ,        2 ,      3 ,       3 ,     6 ,    6 ,        9 ,         12           }
        );
        private static final String[] months = { "январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь" };
        private static final String[] weekdays =  { "воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "выходные" };
        private static final String[][] timeCat = { { "час", "часов" }, { "минута" }, { "секунда" }, { "день" , "дней" , "число" , "дня", "сутки" }, { "неделя" , "недель" }, { "месяц", "месяцев" }, { "год", "лет" }, { "десятилети" }, { "век", "веков" } };
        private static final ArrayList< Pair<String, Integer> > offsets = getPairList(
                new String[] { "прошлый", "предыдущий", "следующий", "последующий", "этот", "текущий", "сегодняшний", "завтрашний", "вчерашний", "позавчерашний", "позапрошлый" },
                new Integer[]{ -1 ,       -1 ,          1 ,          1 ,            0 ,     0 ,        0 ,            1 ,           -1 ,         -2 ,             -2            }
        );
        private static final ArrayList< Pair<String, Integer> > dayTime = getPairList(
                new String[] { "полночь", "утро", "полдень", "вечер", "полночь", "обед" },
                new Integer[]{ 0 ,        8 ,     12 ,       18 ,     0 ,        12     }
        );
        private static final String[] keywords = { "после", "поза", "с", "до", "через", "включительно", "перед", "через", "назад", "сейчас", "сегодня", "завтра", "вчера"};
        private enum Category { NONE, FLOAT, INTEGER, THOUSAND, MONTH, WEEKDAY, DAYTIME, CATEGORY, OFFSET, KEYWORD }
        private static class Result{
            Category category;
            Object object;
            static Result None = new Result(Category.NONE, null);
            Result(Category category, Object object){
                this.category = category; this.object = object;
            }
            @Override
            public String toString(){ return object.toString(); }
            Number getNumber(){ return (Number) object; }
            @Override
            public Result clone(){
                return new Result(category, object);
            }
        }
        private static float relative(String word, String[] array){
            float res;
            float max=0;
            for(int i=0; i<array.length; i++)
                if((res = PhraseComparison.relative(word, array[i])) > max)
                    max = res;
            return max;
        }
        private static LimitHashMap<String, Result> cache = new LimitHashMap<>(200);
        public static Result check(String word){
            word = word.toLowerCase();
            Result result = cache.get(word);
            if(result!=null) return result.clone();

            Pair<Result, Float> max = new Pair<>(Result.None, 0.65f);
            float res;
            for(int i=0; i<tenDigit.length; i++)
                if((res = PhraseComparison.relative(word, tenDigit[i])) > max.two)
                    max = new Pair<>(new Result(Category.INTEGER, i+11), res);
            for(int i=0; i<dozens.length; i++)
                if((res = PhraseComparison.relative(word, dozens[i])) > max.two)
                    max = new Pair<>(new Result(Category.INTEGER, (1+i)*10), res);
            for(int i=0; i<hundreds.length; i++)
                if((res = relative(word, hundreds[i])) > max.two)
                    max = new Pair<>(new Result(Category.INTEGER, (1+i)*100), res);
            for(int i=0; i<digits.length; i++)
                if((res = relative(word, digits[i])) > max.two)
                    max = new Pair<>(new Result(Category.INTEGER, i), res);
            for(Pair<String, Double> fl : floats)
                if((res = PhraseComparison.relative(word, fl.one)) > max.two)
                    max = new Pair<>(new Result(Category.FLOAT, fl.two), res);
            for(Pair<String, Integer> th : thousands)
                if((res = PhraseComparison.relative(word, th.one)) > max.two)
                    max = new Pair<>(new Result(Category.THOUSAND, Math.pow(10, th.two)), res);
            for(Pair<String, Integer> dt : dayTime)
                if((res = PhraseComparison.relative(word, dt.one)) > max.two)
                    max = new Pair<>(new Result(Category.DAYTIME, dt.two), res);
            for(int i=0; i<months.length; i++)
                if((res = PhraseComparison.relative(word, months[i])) > max.two)
                    max = new Pair<>(new Result(Category.MONTH, i), res);
            for(int i=0; i<weekdays.length; i++)
                if((res = PhraseComparison.relative(word, weekdays[i])) > max.two)
                    max = new Pair<>(new Result(Category.WEEKDAY, i), res);
            for(int i=0; i<timeCat.length; i++)
                if((res = relative(word, timeCat[i])) > max.two)
                    max = new Pair<>(new Result(Category.CATEGORY, i), res);
            for(Pair<String, Integer> of : offsets)
                if((res = PhraseComparison.relative(word, of.one)) > max.two)
                    max = new Pair<>(new Result(Category.OFFSET, of.two), res);
            for(int i=2; i<keywords.length; i++)
                if((res = PhraseComparison.relative(word, keywords[i])) > max.two)
                    max = new Pair<>(new Result(Category.KEYWORD, keywords[i]), res);

            cache.put(word, max.one.clone());
            return max.one;
        }
    }

    public static long parseInteger(ArrayList<String> words, boolean[] used){
        Double result = parseNumber(words, used);
        return result!=null ? result.longValue() : null;
    }
    /*private static ArrayList< Pair<String, Integer> > numerals = getPairList(
            new String[] { "вос", "вто", "дев", "дес", "два", "две", "дво", "дву", "оди", "одн", "пар", "пер", "пят", "раз", "сем", "сед", "сор", "сот", "сто", "тре", "три", "тро", "чет", "шес" },
            new Integer[]{   8 ,    2 ,    9 ,    1 ,    2 ,    2 ,    2 ,    2 ,    1 ,    1 ,    2 ,    1 ,    5 ,    1 ,    7 ,    7 ,    4 ,    1 ,    1 ,    3 ,    3 ,    3 ,    4 ,    6   }
    );*/
    private static boolean correctOrder(Long a, Long b){
        return a.toString().length() < b.toString().length() && !(a < 10 && b < 20);
    }
    public static Double parseNumber(ArrayList<String> words, boolean[] used){
        // пять пять пять пять -> 5555
        ArrayList<Integer> digitUsed  = new ArrayList<>(words.size());
        ArrayList<WordFinder.Result> digitParser = new ArrayList<>();  // it's became null when we cannot parse it by single digits
        boolean digitParser_isPreviousANumber = false;  // number or garbage
        int digitParser_lastDigit = 0;

        // пять тысяч пятьсот пятьдесят пять -> 5555
        ArrayList<Integer> orderUsed = new ArrayList<>(words.size());
        ArrayList<WordFinder.Result> orderParser = new ArrayList<>();  // it's became null when we cannot parse it by ordering
        boolean orderParser_isPreviousANumber = false;  // number or thousand
        int orderParser_lastOrderPos = 0;

        Number num;
        for (int i = 0; i < words.size(); i++){
            if(used[i]) continue;

            String word = words.get(i);
            // maybe it's not a word but an integer written in symbols
            Double d = null;
            try {
                d = Double.parseDouble(word);
            } catch (Exception e){ }
            if(d != null){
                Long a = (long) Math.floor(d);

                if (orderParser != null) {
                    if (orderParser.size() > 0 && !correctOrder(a, orderParser.get(orderParser.size() - 1).getNumber().longValue()))
                        break;

                    if (d == Math.floor(d))
                        orderParser.add(new WordFinder.Result(WordFinder.Category.INTEGER, d.longValue()));
                    else orderParser.add(new WordFinder.Result(WordFinder.Category.FLOAT, d));
                    orderUsed.add(i);
                }

                digitParser = null;
                orderParser_isPreviousANumber = true;
                continue;
            }

            if (word.length() < 3) continue; // there's no words in language that can be part of number and is shorter than three characters

            WordFinder.Result result = WordFinder.check(word);
            switch(result.category){
                case THOUSAND :{
                    if(orderParser==null) break;
                    long next = result.getNumber().longValue();
                    if (orderParser_isPreviousANumber) {
                        for (; orderParser_lastOrderPos < orderParser.size(); orderParser_lastOrderPos++){
                            num = (Number) orderParser.get(orderParser_lastOrderPos).object;
                            num = num.doubleValue()*next;
                            orderParser.set(orderParser_lastOrderPos, new WordFinder.Result(WordFinder.Category.INTEGER, num.longValue()));
                        }
                    } else {
                        if (orderParser.size() > 0 && orderParser.get(orderParser.size() - 1).toString().length() <= Long.toString(next).length())
                            break;
                        result.category = WordFinder.Category.INTEGER;
                        orderParser.add(result.clone());
                        orderParser_lastOrderPos = orderParser.size();
                    }
                    orderUsed.add(i);
                    digitParser = null;
                    orderParser_isPreviousANumber = false;
                } break;

                case INTEGER : {
                    num = (Number) result.object;
                    if (orderParser!=null) {
                        WordFinder.Result last = orderParser.size()>0 ? orderParser.get(orderParser.size() - 1) : null;
                        if (orderParser.size() > 0){
                            if(!correctOrder(num.longValue(), last.getNumber().longValue()) || last.category == WordFinder.Category.FLOAT) {
                                if (digitParser != null)  orderParser = null;
                                else {
                                    i = words.size();
                                    break;
                                }
                            } else orderParser.add(result.clone());
                        } else orderParser.add(result.clone());
                        orderParser_isPreviousANumber = true;
                        orderUsed.add(i);
                    }

                    if (digitParser!=null) {
                        WordFinder.Result last = digitParser.size()>0 ? digitParser.get(digitParser.size() - 1) : null;
                        if (digitParser.size() > 0 && digitParser_isPreviousANumber) {
                            if (correctOrder(num.longValue(), (long) digitParser_lastDigit))
                                last.object = last.getNumber().longValue() + num.longValue();
                            else digitParser.add(result.clone());
                        } else digitParser.add(result.clone());
                        digitParser_isPreviousANumber = true;
                        digitUsed.add(i);
                        digitParser_lastDigit = num.intValue();
                    }
                } break;

                case FLOAT : {
                    if(orderParser_lastOrderPos==i-1) break;
                    if(orderParser!=null){
                        orderParser.add(result.clone());
                        orderUsed.add(i);
                    } else break;
                    digitParser = null;
                } break;

                case NONE : {
                    digitParser_isPreviousANumber = false;
                    continue;
                }

                default: break;
            }
            if(digitParser==null && orderParser==null) break;
        }

        if (digitParser==null && orderParser==null)
            return null;
        double ret = 0;
        if (digitParser!=null && (orderParser==null && orderUsed.size()<digitUsed.size())) {
            if(digitParser.size()==0) return null;
            StringBuilder result = new StringBuilder();
            for (WordFinder.Result i : digitParser)
                result.append( ((Long) i.getNumber().longValue()).toString() );
            for (Integer i : digitUsed)
                used[i] = true;
            return Double.parseDouble(result.toString());
        } else {
            if(orderParser.size()==0) return null;
            for (WordFinder.Result i : orderParser)
                ret = ret + i.getNumber().doubleValue();
            for (Integer i : orderUsed)
                used[i] = true;
            return ret;
        }
    }
    private static Pattern pattern = Pattern.compile("([0-9]+(?![0-9]))");
    private static Calendar fromIntegerArray(Calendar calendar, ArrayList<Integer> array, String pattern){
        for(int i=0;i<array.size();i++){
            switch (pattern.charAt(i)){
                case 'h': calendar.set(Calendar.HOUR_OF_DAY, array.get(i)); break;
                case 'm': calendar.set(Calendar.MINUTE, array.get(i)); break;
                case 's': calendar.set(Calendar.SECOND, array.get(i)); break;
                case 'l': calendar.set(Calendar.MILLISECOND, array.get(i)); break;
                case 'D': calendar.set(Calendar.DAY_OF_MONTH, array.get(i)); break;
                case 'M': calendar.set(Calendar.MONTH, array.get(i)-1); break;
                case 'Y': calendar.set(Calendar.YEAR, formatYear(array.get(i))); break;
            }
        }
        return calendar;
    }
    private static boolean fillArrayFromPattern(ArrayList<Integer> array, String word, String patternString){
        if (word.matches(patternString)) {
            Matcher matcher = pattern.matcher(word);
            while (matcher.find())
                array.add(Integer.parseInt(matcher.group()));
            return true;
        }
        return false;
    }
    private static String getDatePatternFromArray(ArrayList<Integer> list){
        if (list.size()>0 && list.get(0) > 60) {
            if (list.size() > 2 && list.get(2) > 11)
                return "YMDhmsl";
            else return "YDMhmsl";
        } else if (list.size()>1 && list.get(1) > 60) {
            if (list.get(0)> 11)
                return "DYMhmsl";
            else return "MYDhmsl";
        } else {
            if (list.size()>2 && list.get(1) > 11)
                return "MDYhmsl";
            else return "DMYhmsl";
        }
    }
    public static long parseDate(ArrayList<String> words, boolean[] used){
        return parseDateImpl(words, used, false);
    }
    public static long parseDateTime(ArrayList<String> words, boolean[] used){
        return parseDateImpl(words, used, true);
    }
    public static long parseTime(ArrayList<String> words, boolean[] used){
        return parseDateImpl(words, used, true);
    }
    private static long parseDateImpl(ArrayList<String> words, boolean[] used, boolean focusOnTime){
        // if input string is just numbers like "02 01 2010", "18:00" and so on
        ArrayList<Integer> dateList = new ArrayList<>();
        boolean yearSet = false;
        int timeSet = 0, dateSet = 0;
        Calendar cal = Calendar.getInstance();
        boolean[] numbersUsage = arrayCopy(used, 0, used.length);
        for(int i=0;i<words.size();i++) {
            if (used[i]) continue;
            ArrayList<Integer> tempList = new ArrayList<>();
            if (fillArrayFromPattern(tempList, words.get(i), "^[0-9]{1,2}\\:[0-9]{1,2}\\:[0-9]{1,2}\\.[0-9]+$") ||  // hh:mm:ss, hh:mm
                    fillArrayFromPattern(tempList, words.get(i), "([0-9]{1,2}[\\:]){1,2}[0-9]{1,2}$")) {  // hh:mm:ss.lll
                if (tempList.size() < 2) {
                    dateList.addAll(tempList);
                    continue;
                }
                if (timeSet > 0) break;
                numbersUsage[i] = true;
                timeSet = tempList.size();
                switch (timeSet) {
                    case 4: {
                        Integer ms = tempList.get(3);
                        while (ms < 100) ms *= 10;
                        while (ms > 1000) ms /= 10;
                    }
                    case 3:
                    case 2:
                        cal = fromIntegerArray(cal, tempList, "hmsl");
                        break;
                    default:
                        break;
                }
            } else if (fillArrayFromPattern(tempList, words.get(i), "([0-9]{1,2}[\\\\\\/\\.\\-]){1,2}[0-9]{1,2}$")) { // DD\MM\YYYY
                if (tempList.size() < 2) {
                    dateList.addAll(tempList);
                    continue;
                }
                if (dateSet > 0) break;
                numbersUsage[i] = true;
                dateSet = tempList.size();
                String pattern = getDatePatternFromArray(tempList);
                if (pattern.charAt(2) != 'Y' || tempList.size() > 2) yearSet = true;
                cal = fromIntegerArray(cal, tempList, pattern);
            }
        }
        for(int i=0;i<words.size();i++) {
            if (used[i] || numbersUsage[i]) continue;
            if (fillArrayFromPattern(dateList, words.get(i), "[0-9\\\\\\/\\.\\-\\s\\:]+[\\\\\\/\\.\\-\\s\\:][0-9\\\\\\/\\.\\-\\s\\:]+")){ // something looks like date, but wrong format
                numbersUsage[i]=true;
            } else {
                try {
                    dateList.add(Integer.parseInt(words.get(i)));
                    numbersUsage[i]=true;
                } catch (Exception e) { break; }
            }
        }

        if(dateSet>0){
            if(!yearSet)
                for(int i=0;i<dateList.size();i++)
                    if(dateList.get(i)>1000){
                        cal.set(Calendar.YEAR, dateList.get(i));
                        dateList.remove(i);
                    }
            if(timeSet==0) cal = fromIntegerArray(cal, dateList, "hmsl");
            System.arraycopy(numbersUsage, 0, used, 0, used.length);
            return cal.getTimeInMillis();
        }
        String pattern = getDatePatternFromArray(dateList);

        if(dateList.size()>=2) {
            System.arraycopy(numbersUsage, 0, used, 0, used.length);
            switch (dateList.size()) {
                case 2:
                case 3:
                    if (focusOnTime)
                        return fromIntegerArray(cal, dateList, "hms").getTimeInMillis();
                    else return fromIntegerArray(cal, dateList, pattern).getTimeInMillis();
                case 4:
                case 5:
                    if (focusOnTime)
                        return fromIntegerArray(cal, dateList, "hmDMY").getTimeInMillis();
                    else return fromIntegerArray(cal, dateList, pattern).getTimeInMillis();
                case 6:
                    if (focusOnTime)
                        return fromIntegerArray(cal, dateList, "hmsDMY").getTimeInMillis();
                    else return fromIntegerArray(cal, dateList, pattern).getTimeInMillis();
                case 7:
                    if (focusOnTime)
                        return fromIntegerArray(cal, dateList, "hmslDMY").getTimeInMillis();
                    else return fromIntegerArray(cal, dateList, pattern).getTimeInMillis();
            }
        }
        // it's really words. so sad...
        int offsetWordPos=-1, offsetType=-1;  // -1 - нет / 0 - включительно / 1 - до / 2 - до включительно / 3 - после
        int lastWordUsed = words.size();

        for(int i=0;i<words.size();i++){
            if(used[i]) continue;
            WordFinder.Result result = WordFinder.check(words.get(i));
            if(result.category!= WordFinder.Category.KEYWORD) continue;

            String word = (String) result.object;
            // offset words
            if(word.equals("до") || word.equals("перед")){
                if(offsetWordPos==-1 && offsetType<1){
                    offsetWordPos=i;
                    offsetType+=2;
                    used[i]=true;
                } else {
                    lastWordUsed = i+1;
                    break;
                }
            } else if(word.equals("включительно")){
                if(offsetType==1 || offsetType==-1){
                    offsetType++;
                    used[i]=true;
                } else {
                    lastWordUsed = i+1;
                    break;
                }
            } else if(word.equals("после")){
                if(offsetWordPos==-1){
                    offsetWordPos=i;
                    offsetType=4;
                    used[i]=true;
                } else {
                    lastWordUsed = i+1;
                    break;
                }
            }
        }
        if(offsetWordPos>=0){ // constructions like "{some time} before/after {other exact time}"
            ArrayList<String> relativePart = new ArrayList<>(words.subList(0, offsetWordPos));
            boolean[] relativePartUsed = arrayCopy(used,0, relativePart.size());

            ArrayList<String> absolutePart = new ArrayList<>(words.subList(offsetWordPos+1, lastWordUsed));
            boolean[] absolutePartUsed = arrayCopy(used,offsetWordPos+1, absolutePart.size());

            int direction = (offsetType==4 ? 1 : -1);
            DateTimeStruct exactDate = parseExactDate(absolutePart, absolutePartUsed);
            DateTimeStruct relativeDate = parseRelativeDateImpl(relativePart, relativePartUsed, direction);

            System.arraycopy(relativePartUsed,0, used,0, offsetWordPos);
            System.arraycopy(absolutePartUsed,0, used,offsetWordPos+1, lastWordUsed-offsetWordPos-1);

            if(relativeDate.equals(DateTimeStruct.Zero)) relativeDate = new DateTimeStruct(DAY, null);
            if(dateSet>0){
                for(int i=0;i<used.length;i++)
                    used[i] = used[i] | numbersUsage[i];
                Calendar cc = Calendar.getInstance();
                cc.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0,0,0);
                relativeDate.date = cc.getTimeInMillis();
            }
            if(timeSet>0){
                for(int i=0;i<used.length;i++)
                    used[i] = used[i] | numbersUsage[i];
                Calendar cc = Calendar.getInstance();
                cc.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0,0,0);
                relativeDate.time = cal.getTimeInMillis() - cc.getTimeInMillis();
            }
            return exactDate.getTime() + direction * relativeDate.getTime();
        } else {
            int i, start=0, direction=0, wlen=words.size();
            for(i=0; i<words.size(); i++) {
                WordFinder.Result result = WordFinder.check(words.get(i));
                if(result.category==WordFinder.Category.KEYWORD && result.toString().equals("через")) {
                    used[i] = true;
                    direction = 1;
                    i++;
                    start = i;
                    break;
                }
            }
            if(i==words.size())
                for(wlen--; wlen>=0; wlen--) {
                    WordFinder.Result result = WordFinder.check(words.get(wlen));
                    if(result.category==WordFinder.Category.KEYWORD && result.toString().equals("назад")) {
                        used[wlen] = true;
                        direction = -1;
                        i = 0;
                        wlen++;
                        break;
                    }
                }
            if(wlen<0) wlen = words.size();
            if(direction==0){ // it's an exact datetime
                ArrayList<String> absolutePart = new ArrayList<>(words.subList(start, wlen));
                boolean[] absolutePartUsed = arrayCopy(used, start, absolutePart.size());

                DateTimeStruct number = parseExactDate(absolutePart, absolutePartUsed);
                System.arraycopy(absolutePartUsed,0, used,start, wlen-start);
                if(dateSet>0){
                    Calendar cc = Calendar.getInstance();
                    cc.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0,0,0);
                    number.date = cc.getTimeInMillis();
                }
                if(timeSet>0){
                    Calendar cc = Calendar.getInstance();
                    cc.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0,0,0);
                    number.time = cal.getTimeInMillis() - cc.getTimeInMillis();
                }
                return number.getTime();
            } else { // it's a relative datetime, counting from today
                ArrayList<String> relativePart = new ArrayList<>(words.subList(start, wlen));
                boolean[] relativePartUsed = arrayCopy(used, start, relativePart.size());

                DateTimeStruct number = parseRelativeDateImpl(relativePart, relativePartUsed, direction);
                System.arraycopy(relativePartUsed,0, used,start, wlen-start);
                return Calendar.getInstance().getTimeInMillis()+number.getTime()*direction;
            }
        }
    };

    private static Pair<Integer, Integer> parseYesterdayTomorrow(ArrayList<String> words, boolean[] used){  // послезавтра, позавчера
        int counter=0;
        boolean found = false;
        for(int i=0;i<words.size();i++){
            if(used[i]) continue;

            String word = words.get(i).toLowerCase();
            WordFinder.Result result = WordFinder.check(word);

            if(result.category != WordFinder.Category.KEYWORD) {
                if (result.category == WordFinder.Category.NONE){
                    float res=2, r;
                    if((r = PhraseComparison.borderedAbsolute(word, "после", 5)) < res) {
                        res = r;
                        result.object = "после";
                    }
                    if((r = PhraseComparison.borderedAbsolute(word, "поза", 4)) < res) {
                        res = r;
                        result.object = "поза";
                    }
                    if(res==2) continue;
                } else break;
            }

            switch(result.toString()){
                case "сейчас": {
                    used[i] = true;
                    return new Pair<>(1, 0);
                }
                case "сегодня": {
                    used[i] = true;
                    return new Pair<>(2, 0);
                }
                case "поза": {
                    do {
                        counter--;
                        if (word.length() < 5) break;
                        word = word.substring(4);
                    } while (PhraseComparison.borderedAbsolute(word, "поза") < 2);
                    used[i] = true;
                    if (PhraseComparison.absolute(word, "вчера") == 0) {
                        counter--;
                        i = words.size();
                        found = true;
                    }
                } break;
                case "вчера": {
                    if (counter <= 0) {
                        used[i] = true;
                        counter--;
                        found = true;
                    }
                    i = words.size();
                }
                case "после": {
                    do {
                        counter++;
                        if (word.length() < 6) break;
                        word = word.substring(5);
                    } while (PhraseComparison.borderedAbsolute(word, "после") < 2);
                    used[i] = true;
                    if (PhraseComparison.absolute(word, "завтра") < 2) {
                        counter++;
                        i = words.size();
                        found = true;
                    }
                } break;
                case "завтра": {
                    if (counter >= 0) {
                        used[i] = true;
                        counter++;
                        found = true;
                    }
                    i = words.size();
                }
            }
        }
        if(found && counter==0)
            return new Pair<>(0, 0);
        return new Pair<>(2, counter);
    }
    private static class OffsetNumber {
        int pos;
        boolean absolute;
        double number;
        OffsetNumber(int p, double n, boolean abs){
            pos = p; absolute = abs; number = n;
        }
        void print(){
            System.out.println("position="+pos+", number="+number+", is absolute="+absolute);
        }
    }
    private static class OffsetWord {
        int pos;
        boolean exact;
        int type;
        OffsetWord(int p, int t){
            pos = p; type = t; exact = true;
        }
        OffsetWord(int p){
            pos = p; exact = false; type = 0;
        }
        public void print(){
            System.out.println("position="+pos+", type="+type+", is exact="+exact);
        }
    }
    private static DateTimeStruct parseExactDate(ArrayList<String> words, boolean[] used){ // exact day
        if(words.size()==0) return new DateTimeStruct(Calendar.getInstance());
        boolean[] ytused = arrayCopy(used, 0, used.length);

        OffsetWord[] offsetWords = new OffsetWord[7];  // hour, minute, second, day, weekday, month, year

        Calendar calendar = Calendar.getInstance();
        Pair<Integer, Integer> n = parseYesterdayTomorrow(words,ytused);
        switch(n.one){
            case 1:
                return new DateTimeStruct(calendar);
            case 2:
                for(int i=0;i<used.length;i++)
                    if(ytused[i] && !used[i]) {
                        System.arraycopy(ytused, 0, used, 0, used.length);
                        calendar.add(Calendar.DATE, n.two);
                        offsetWords[3] = new OffsetWord(i, calendar.get(Calendar.DAY_OF_MONTH));
                        offsetWords[5] = new OffsetWord(i, calendar.get(Calendar.MONTH));
                        offsetWords[6] = new OffsetWord(i, calendar.get(Calendar.YEAR));
                        break;
                    }
                break;
        }

        int lastWordUsed=words.size();
        Integer dayTime = null;
        // locating names of time categories
        for(int i=0;i<lastWordUsed;i++){
            if(used[i]) continue;

            WordFinder.Result result = WordFinder.check(words.get(i));

            if(result.category == WordFinder.Category.DAYTIME) {
                if (dayTime != null) {
                    lastWordUsed = i;
                    break;
                }
                dayTime = result.getNumber().intValue();
            } else if(result.category == WordFinder.Category.CATEGORY) {
                int index = result.getNumber().intValue();
                if(offsetWords[index]==null) offsetWords[index] = new OffsetWord(i);
                else {
                    lastWordUsed = i;
                    break;
                }
            } else if(result.category == WordFinder.Category.WEEKDAY) {
                if (offsetWords[4] == null && offsetWords[3]==null) {
                    offsetWords[4] = new OffsetWord(i, result.getNumber().intValue());
                    continue;
                }
                lastWordUsed = i;
                break;
            } else if(result.category == WordFinder.Category.MONTH) {
                if(offsetWords[5] == null){
                    offsetWords[5] = new OffsetWord(i, result.getNumber().intValue());
                    continue;
                }
                lastWordUsed=i;
                break;
            }
        }

        // System.out.println(offsetWords[0]+" "+offsetWords[1]+" "+offsetWords[2]+" "+offsetWords[3]+" "+offsetWords[4]+" "+offsetWords[5]+" "+offsetWords[6]);

        ArrayList<String> subwords = new ArrayList<>(words.subList(0, lastWordUsed));

        // locating numbers between time categories
        SortedSet<Integer> barriersSet = new TreeSet<>();
        barriersSet.add( -1 );
        barriersSet.add( lastWordUsed );
        ArrayList<OffsetNumber> numbers=new ArrayList<>(0);
        if(offsetWords[5]!=null && offsetWords[5].exact && offsetWords[3]==null && offsetWords[4]==null) offsetWords[3]=new OffsetWord(offsetWords[5].pos);
        if(offsetWords[6]==null && (offsetWords[5]!=null || offsetWords[3]!=null)) offsetWords[6]=new OffsetWord(lastWordUsed);
        for(int i=0; i<7; i++)
            if(offsetWords[i]!=null){
                if(offsetWords[i].pos>=0 && offsetWords[i].pos<lastWordUsed)
                    used[offsetWords[i].pos] = true;

                barriersSet.add(offsetWords[i].pos);
            }

        Integer[] barriers = barriersSet.toArray(new Integer[barriersSet.size()]);
        for(int i=1; i<barriers.length; i++){
            int start = barriers[i-1]+1, end = barriers[i], l;
            // System.out.println("searching number between "+start+" "+end+" / "+!(start >= end || end>subwords.size()));
            if(start >= end || end>subwords.size()) continue;

            ArrayList<String> sw = new ArrayList<>(subwords.subList(start, end));
            do {
                boolean[] tu = arrayCopy(used,start, sw.size());
                OffsetNumber number;
                int pos=-1;
                WordFinder.Result result = null;
                for(int k=start, kp=0; k<end; k++, kp++) {
                    if(tu[kp]) continue;
                    WordFinder.Result r = WordFinder.check(words.get(k));
                    if(r.category == WordFinder.Category.OFFSET){
                        result = r;
                        pos = kp;
                        break;
                    }
                }
                if(result==null) {
                    Double num = parseNumber(sw, tu);
                    if(num==null) break;
                    number = new OffsetNumber(0, num, true);
                } else {
                    tu[pos]=true;
                    number = new OffsetNumber(0, result.getNumber().doubleValue(), false);
                }

                l=0;
                for(int k=0;k<tu.length;k++){
                    if(!used[k+start] && tu[k])
                        l=k;
                    used[k+start] = tu[k] || used[k+start];
                }

                number.pos = start+l;
                // number.print();
                numbers.add(number);
            } while(numbers.size()<7);
        }

        //linking numbers and categories
        ArrayList<Pair<Integer, OffsetWord>> ooooh=new ArrayList<>();

        // System.out.println(offsetWords[0]+" "+offsetWords[1]+" "+offsetWords[2]+" "+offsetWords[3]+" "+offsetWords[4]+" "+offsetWords[5]+" "+offsetWords[6]);
        for(int i=0;i<offsetWords.length;i++)
            ooooh.add(new Pair<>(i,offsetWords[i]));
        int len = ooooh.size(), nlen = numbers.size(), max=words.size()*words.size();
        int[] permutation=new int[len];
        int[] finalPermutation=new int[len];
        for(int i=0;i<len;i++) permutation[i]=finalPermutation[i]=i;

        do {
            int u=0, d;
            // every permutation gets score
            for(d=0;d<len && d<nlen;d++) {
                if (ooooh.get(permutation[d]).two != null && ooooh.get(permutation[d]).two.pos>=0) {
                    u += Math.abs(numbers.get(d).pos - ooooh.get(permutation[d]).two.pos);
                    if (numbers.get(d).pos>ooooh.get(permutation[d]).two.pos)
                        u++;
                    if (ooooh.get(permutation[d]).two.exact && ooooh.get(permutation[d]).one==5)
                        u+=words.size();
                } else u+=2;
                if (numbers.get(d).number > 50 && ooooh.get(permutation[d]).one != 6)
                    u += max;
            }
            for(;d<len;d++)
                if(ooooh.get(permutation[d]).two!=null && !ooooh.get(permutation[d]).two.exact) u++;

            if(u==0) break;

            if(u>0 && u<max){
                finalPermutation=permutation.clone();
                max=u;
            }

            // getting next permutation
            int[] tps=permutation.clone();
            boolean found = false;
            do {
                int i = len;
                do {
                    if(i<2){
                        found = true;
                        break;
                    }
                    --i;
                } while (permutation[i - 1] > permutation[i]);
                if (found) break;

                int j = len;
                while (i < j && permutation[i - 1] > permutation[--j]) ;
                u = permutation[i - 1];
                permutation[i - 1] = permutation[j];
                permutation[j] = u;
                j = len;
                while (i < --j) {
                    u = permutation[i];
                    permutation[i] = permutation[j];
                    permutation[j] = u;
                    i++;
                }
            } while( Arrays.equals( Arrays.copyOfRange(permutation,0,nlen), Arrays.copyOfRange(tps,0,nlen) ) );
            if (found) break;
        } while(true);


        double year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH),
                day = calendar.get(Calendar.DAY_OF_MONTH);

        boolean yearSet=false, monthSet=false, daySet=false;

        OffsetNumber dayNumber = null, weekdayNumber = null, monthNumber = null, yearNumber = null, hourNumber = null, minuteNumber = null, secondNumber = null;
        for(int i=0;i<finalPermutation.length && i<numbers.size();i++){
            // System.out.print(finalPermutation[i]+" ");
            switch (ooooh.get(finalPermutation[i]).one){
                case 0:    hourNumber = numbers.get(i); break;
                case 1:  minuteNumber = numbers.get(i); break;
                case 2:  secondNumber = numbers.get(i); break;
                case 3:     dayNumber = numbers.get(i); break;
                case 4: weekdayNumber = numbers.get(i); break;
                case 5:   monthNumber = numbers.get(i); break;
                case 6:    yearNumber = numbers.get(i); break;
            }
        }
        // setting year
        if(yearNumber != null){
            yearSet = true;
            if(yearNumber.absolute)
                year = formatYear((int) yearNumber.number);
            else year += yearNumber.number;
        }

        // setting month
        if(offsetWords[5] != null) {
            if (!offsetWords[5].exact){
                if(monthNumber != null) {
                    monthSet = true;
                    if (monthNumber.absolute)
                        month = monthNumber.number;
                    else month = month + monthNumber.number;
                    while (month < 0) {
                        month += 12;
                        if (!yearSet) year--;
                    }
                    while (month > 11) {
                        month -= 12;
                        if (!yearSet) year++;
                    }
                }
            } else {
                monthSet = true;
                month = offsetWords[5].type;
                if(dayNumber == null && (offsetWords[3]==null || !offsetWords[3].exact) && (offsetWords[4]==null || !offsetWords[4].exact))
                    day = 1;
            }
        }
        // setting day
        if(offsetWords[4] != null){
            daySet = true;
            Calendar t = Calendar.getInstance();

            if(weekdayNumber != null){
                boolean sameDay = (t.get(Calendar.DAY_OF_WEEK)-1 == offsetWords[4].type);
                if (weekdayNumber.absolute) {
                    t.set((int) year, (int) month, 1);
                    weekdayNumber.number--;
                } else {
                    t.set((int) year, (int) month, (int) day);
                    if(!sameDay && weekdayNumber.number==1) weekdayNumber.number=0;
                }

                while(t.get(Calendar.DAY_OF_WEEK)-1 != offsetWords[4].type)
                    t.add(Calendar.DATE, 1);


                t.add(Calendar.DATE, (int) weekdayNumber.number * 7);
            } else {
                t.set((int) year, (int) month, (int) day);
                while(t.get(Calendar.DAY_OF_WEEK)-1 != offsetWords[4].type)
                    t.add(Calendar.DATE, 1);
            }

            month=t.get(Calendar.MONTH);
            day=t.get(Calendar.DAY_OF_MONTH);
            year=t.get(Calendar.YEAR);

        } else if(dayNumber != null){
            daySet = true;
            Calendar t = Calendar.getInstance();
            if (dayNumber.absolute) {
                t.set((int) year, (int) month, 1);
                t.add(Calendar.DATE, (int) dayNumber.number - 1);
            } else {
                t.set((int) year, (int) month, (int) day);
                t.add(Calendar.DATE, (int) dayNumber.number);
            }

            month=t.get(Calendar.MONTH);
            day=t.get(Calendar.DAY_OF_MONTH);
            year=t.get(Calendar.YEAR);
        }

        calendar.set((int)year, (int)month, (int)day);
        boolean timeSet = false, dateSet = monthSet | daySet | yearSet;

        // setting hour
        if (hourNumber!=null) {
            timeSet = true;
            if (hourNumber.absolute) {
                if(dayTime!=null && dayTime>12 && hourNumber.number < 13) hourNumber.number+=12;
                calendar.set(Calendar.HOUR_OF_DAY, (int) hourNumber.number);
            } else calendar.add(Calendar.HOUR_OF_DAY, (int) hourNumber.number);
            if(!dateSet && calendar.getTimeInMillis()<Calendar.getInstance().getTimeInMillis()) calendar.add(Calendar.DATE, 1);
        } else if (dayTime!=null)
            calendar.set(Calendar.HOUR_OF_DAY, dayTime);
        else calendar.set(Calendar.HOUR_OF_DAY, 12);
        // setting minute
        if (minuteNumber!=null) {
            timeSet = true;
            if (minuteNumber.absolute)
                calendar.set(Calendar.MINUTE, (int) minuteNumber.number);
            else calendar.add(Calendar.MINUTE, (int) minuteNumber.number);
            if(!dateSet && calendar.getTimeInMillis()<Calendar.getInstance().getTimeInMillis()) calendar.add(Calendar.DATE, 1);
        } else calendar.set(Calendar.MINUTE, 0);
        // setting second
        if (secondNumber!=null) {
            timeSet = true;
            if (secondNumber.absolute)
                calendar.set(Calendar.SECOND, (int) secondNumber.number);
            else calendar.add(Calendar.SECOND, (int) secondNumber.number);
            if(!dateSet && calendar.getTimeInMillis()<Calendar.getInstance().getTimeInMillis()) calendar.add(Calendar.DATE, 1);
        } else calendar.set(Calendar.SECOND, 0);

        //System.out.println("absolute: "+(int) day+" "+(int) (month+1)+" "+(int) year);
        if(timeSet) return new DateTimeStruct(calendar);
        return DateTimeStruct.fromDate(calendar);
    }
    private static long getMsToAdd(int type, double multiplier, int direction) {
        double ticksToAdd=0;
        switch(type){
            case 0: ticksToAdd=3600000*multiplier; break;
            case 1: ticksToAdd=60000*multiplier; break;
            case 2: ticksToAdd=1000*multiplier; break;
            case 3: ticksToAdd=DAY*multiplier; break;
            case 4: ticksToAdd=DAY*7*multiplier; break;
            case 5: ticksToAdd=DAY*Calendar.getInstance().getMaximum(Calendar.DAY_OF_MONTH)*multiplier; break;
            case 6: ticksToAdd=DAY*Calendar.getInstance().getMaximum(Calendar.DAY_OF_YEAR)*multiplier; break;
            case 7: ticksToAdd=DAY*3652*multiplier; break;
            case 10: case 11: case 12: case 13: case 14: case 15: case 16: {
                type-=10;
                Calendar cal = Calendar.getInstance();
                while( cal.get(Calendar.DAY_OF_WEEK)-1 != type){
                    cal.add(Calendar.DATE, direction);
                    ticksToAdd+=DAY;
                }
                ticksToAdd+=DAY*7*multiplier;
            } break;
        }
        return (long) ticksToAdd;
    }
    public static long parseRelativeDateTime(ArrayList<String> words, boolean[] used){
        return parseRelativeDateImpl(words, used, 1).getTime();
    }
    private static DateTimeStruct parseRelativeDateImpl(ArrayList<String> words, boolean[] used, int direction){ //через
        if(words.size()==0) return DateTimeStruct.Zero;
        boolean[] usedTypes=new boolean[17];
        int lastCategoryType = -1, start = 0;
        long timeToAdd = 0;
        for(int i=0;i<words.size();i++){
            if(used[i]) continue;

            WordFinder.Result result = WordFinder.check(words.get(i));

            int foundType;
            if(result.category == WordFinder.Category.CATEGORY)
                foundType = result.getNumber().intValue();
            else if(result.category == WordFinder.Category.WEEKDAY)
                foundType = result.getNumber().intValue()+10;
            else if(result.category == WordFinder.Category.THOUSAND
                    || result.category == WordFinder.Category.INTEGER
                    || result.category == WordFinder.Category.FLOAT
                    || result.category == WordFinder.Category.NONE)
                continue;
            else break;

            if(usedTypes[foundType]){
                if(lastCategoryType<0) break;
                foundType = -1;
            } else usedTypes[foundType]=true;

            used[i]=true;
            if(i == start){
                if(lastCategoryType>=0) {
                    //System.out.println("1: "+lastCategoryType+" "+getDaysToAdd(lastCategoryType, 1));
                    timeToAdd += getMsToAdd(lastCategoryType, 1, direction);
                }
                lastCategoryType=foundType;
                start=i+1;
                continue;
            }
            Double num = null;
            do {
                int currentType, len=i-start;
                if(len<1) break;

                if(lastCategoryType==-1)
                    currentType=foundType;
                else if(lastCategoryType >= 0){
                    currentType=lastCategoryType;
                    if(len<0) len=1;
                } else break;
                ArrayList<String> sw = new ArrayList<>(words.subList(start, len-start));
                boolean[] tu = arrayCopy(used, start, len);
                num = parseNumber(sw, tu);
                if(num == null){
                    if(lastCategoryType>-1) {
                        start = i + 1;
                        num = 1.0;
                    } else break;
                }

                int l=0;
                for(int k=0;k<tu.length;k++){
                    //System.out.print((k+start)+" "+tu[k]+" "+used[k+start]+" / ");
                    used[k+start]=tu[k] || used[k+start];
                    if(tu[k]) l=k;
                }

                start += l+1;
                if(lastCategoryType<0) continue;

                //System.out.println("2: "+lastCategoryType+" "+getDaysToAdd(currentType, num));
                timeToAdd+=getMsToAdd(currentType, num, direction);
                num = null;
                if(lastCategoryType>=0) lastCategoryType=-1;

            } while(lastCategoryType>=0 || foundType>=0);

            start=i+1;
            if(lastCategoryType<0 && foundType>=0){
                //System.out.println("3: "+lastCategoryType+" "+getDaysToAdd(foundType, num<0 ? 1 : num));
                timeToAdd+=getMsToAdd(foundType, num!=null ? num : 1, direction);
            }
        }
        if(lastCategoryType>=0){
            int len=words.size()-start;
            ArrayList<String> sw = new ArrayList<>(words.subList(start, len+start));
            boolean[] tu = arrayCopy(used, start, len);
            System.arraycopy(used,start, tu,0, len);

            Double num = parseNumber(sw,tu);

            for(int k=0;k<tu.length;k++)
                used[k+start]=tu[k] || used[k+start];
            timeToAdd+=getMsToAdd(lastCategoryType, num!=null ? num : 1, direction);
        }

        // System.out.println("relative: "+timeToAdd);
        long time = timeToAdd % DAY;
        return new DateTimeStruct(timeToAdd - time, time);
    }

    private static <K, V> ArrayList< Pair<K, V> > getPairList(K[] keys, V[] values){
        ArrayList< Pair<K, V> > list = new ArrayList<>();
        for(int i=0;i<keys.length;i++)
            list.add(new Pair<>(keys[i], values[i]));
        return list;
    }
    private static boolean equalStart(String one, String two){
        return one.charAt(0)==two.charAt(0);
    }
    private static Calendar calendarFromTime(int hour, int minute, int second, int ms){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, ms);
        return cal;
    }
    private static Calendar calendarFromDate(int day, int month, int year){
        Calendar cal = Calendar.getInstance();
        cal.set(day, month, year);
        return cal;
    }
    private static Calendar calendarFromDate(int day, int month, int year, int hour, int minute, int second, int ms){
        Calendar cal = Calendar.getInstance();
        cal.set(day, month, year, hour, minute, second);
        cal.set(Calendar.MILLISECOND, ms);
        return cal;
    }
    private static int formatYear(int year){
        if(year>1000) year=year;
        else if(year>100) year=year+1000;
        else if(year>50) year=year+1900;
        else if(year>0) year=year+2000;
        return year;
    }
    private static boolean[] arrayCopy(boolean[] from, int start, int len){
        boolean[] array = new boolean[len];
        System.arraycopy(from, start, array, 0, len);
        return array;
    }
}