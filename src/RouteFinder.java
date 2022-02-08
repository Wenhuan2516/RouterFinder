import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RouteFinder implements IRouteFinder{

    @Override
    public Map<String, Map<String, String>> getBusRoutesUrls(char destInitial) throws IOException {
        Map<String, Map<String, String>>  outerMap = new HashMap<>();
        String text = getUrlText(IRouteFinder.TRANSIT_WEB_URL);

        Pattern getDestination = Pattern.compile("<h3>((?i)" + destInitial + ".*?)</h3>([\\s\\S]*?)<hr[\\s\\S]*?/>");
        Matcher destMatcher = getDestination.matcher(text);

        while (destMatcher.find()) {
            Map<String, String> innerMap = new HashMap<>();
            String destination = destMatcher.group(1);
            String busUrls = destMatcher.group(2);

            String busRegex = "<strong><a href=\"/schedules/(.*)\".*?>(.*)</a></strong>";
            Pattern checkBusUrl = Pattern.compile(busRegex);
            Matcher busUrl = checkBusUrl.matcher(busUrls);

            while (busUrl.find()) {
                String routeUrl = busUrl.group(1);
                String busNumber = busUrl.group(2);

                busUrls = IRouteFinder.TRANSIT_WEB_URL + routeUrl;
                innerMap.put(busNumber, busUrls);
            }
            outerMap.put(destination, innerMap);
        }
        return outerMap;
    }

    @Override
    public Map<String, List<Long>> getBusRouteTripsLengthsInMinutesToAndFromDestination(Map<String, String> destinationBusesMap) throws IOException {
        Map<String, List<Long>> finalResult = new HashMap<>();
        for (Map.Entry<String, String> entry : destinationBusesMap.entrySet()) {
            String busNumber = entry.getKey();
            String busUrl = entry.getValue();
            String text = getUrlText(busUrl);

            String tableRegex = "<table[\\s\\S]*?>([\\s\\S]*?)</table>";
            String weekdayRegex = "<h2>([\\s\\S]*?)</table>";
            String destRegex = "Weekday<small>(.*)</small></h2>"; //to find direction
            String rowRegex = "<tr>([\\s\\S]*?)</tr>";
            String timeRegex = "(\\d{1,2}:\\d{1,2}\\s(A|P)M)";

            Pattern tablePattern = Pattern.compile(tableRegex);
            Pattern weekdayPattern = Pattern.compile(weekdayRegex);
            Pattern rowPattern = Pattern.compile(rowRegex);
            Pattern destPattern = Pattern.compile(destRegex);
            Pattern timePattern = Pattern.compile(timeRegex);

            Matcher tableMatch = tablePattern.matcher(text);

            while (tableMatch.find()) {
                String tableInfo = tableMatch.group();
                Matcher weekdaySchedule = weekdayPattern.matcher(tableInfo);

                String keys = "";
                List<Long> lengthList = new ArrayList<>();
                while (weekdaySchedule.find()) {
                    String schedule = weekdaySchedule.group();
                    Matcher rowMatch = rowPattern.matcher(schedule);
                    lengthList = new ArrayList<>();
                    String direction = "";
                    Matcher destMatcher = destPattern.matcher(schedule);

                    while (destMatcher.find()) {
                        direction = destMatcher.group(1);
                    }
                    if ( direction != "") {
                        keys = busNumber + "-" + direction;
                    }

                    long length = 0;
                    while (rowMatch.find()) {
                        String rows = rowMatch.group();
                        Matcher timeMatcher = timePattern.matcher(rows);

                        ArrayList<String> timeList = new ArrayList<>();
                        while (timeMatcher.find()) {
                            String times = timeMatcher.group();
                            timeList.add(times);
                        }
                        if ( timeList.size() > 1) {
                            length = tripLengthCalculator(timeList.get(0), timeList.get(timeList.size()-1));
                            if ( length > 0) {
                                lengthList.add(length);
                            }
                        }
                    }
                }
                if ( keys != "") {
                    finalResult.put(keys, lengthList);
                }
            }
        }
        return finalResult;
    }

    private String getUrlText(String str) throws IOException {
        URLConnection schedules = new URL(str).openConnection();

        schedules.setRequestProperty("user-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        BufferedReader in = new BufferedReader(new InputStreamReader(schedules.getInputStream()));

        String inputLine = "";

        String text = "";
        while ((inputLine = in.readLine()) != null) {
            text += inputLine + "\n";

        }

        return text;
    }

    private long tripLengthCalculator(String start, String arrival) {
        String startTime = start.substring(0, start.length()-3);
        String arrivalTime = arrival.substring(0, arrival.length()-3);
        String[] hm1 = startTime.split(":");
        String[] hm2 = arrivalTime.split(":");
        int[] hours1 = new int[2];
        int[] hours2 = new int[2];
        for (int i = 0; i < 2; i++) {
            if ( arrival.substring(arrival.length()-2).equals("PM") && !hm2[0].equals("12")) {
                hours2[0] = Integer.parseInt(hm2[0]) + 12;
            } else {
                hours2[0] = Integer.parseInt(hm2[0]);
            }

            if ( start.substring(start.length()-2).equals("PM") && !hm1[0].equals("12") ) {
                hours1[0] = Integer.parseInt(hm1[0]) + 12;
            } else {
                hours1[0] = Integer.parseInt(hm1[0]);
            }

            hours1[1] = Integer.parseInt(hm1[1]);
            hours2[1] = Integer.parseInt(hm2[1]);
        }

        int startHour = hours1[0];
        int startMinute = hours1[1];
        int arrivalHour = hours2[0];
        int arrivalMinute = hours2[1];

        int[] tripLength = new int[2];
        long length = 0;

        if ( arrivalMinute < startMinute) {
            arrivalMinute += 60;
            arrivalHour -= 1;
        }
        tripLength[0] = arrivalHour - startHour;
        tripLength[1] = arrivalMinute - startMinute;

        length = tripLength[0]*60 + tripLength[1];

        return length;
    }
}
