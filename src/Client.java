import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.*;



public class Client {
    public static void main(String[] args) throws Exception {
        printBusTripInfo();
        Scanner sc = new Scanner(System.in);
        char yesOrNo;
        System.out.print("Do you want to check different destination?Please type Y to continue or press any other key to exit ");
        yesOrNo = sc.next().charAt(0);
        while ( yesOrNo == 'Y') {
            printBusTripInfo();
            System.out.print("Do you want to check different destination?Please type Y to continue or press any other key to exit ");
                yesOrNo = sc.next().charAt(0);
        }
    }

    static void printBusTripInfo() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter a letter that your destination start with ");

        char dest = sc.next().charAt(0);
        Map<String, Map<String, String>> outerMap = new HashMap<>();

        RouteFinder newMap = new RouteFinder();
        outerMap = newMap.getBusRoutesUrls(dest);

        for (Map.Entry<String, Map<String, String>> entry : outerMap.entrySet()) {
            System.out.println("Destination: " + entry.getKey());
            HashMap<String, String> innerMap = (HashMap<String, String>) entry.getValue();
            for (Map.Entry<String, String> innerEntry: innerMap.entrySet()) {
                System.out.println("Bus Number: " + innerEntry.getKey());
            }
            System.out.println("++++++++++++++++++++++++++++++++++++");
        }

        //print out the trip length list
        System.out.print("Please enter your destination: ");
        String destination = sc.next();
        System.out.println();
        //to make it case insensitive
        String target = destination.substring(0,1).toUpperCase() + destination.substring(1).toLowerCase();
        if ( outerMap.containsKey(target)) {
            Map<String, String> locationMap = outerMap.get(target);
            Map<String, List<Long>> busTripMap = newMap.getBusRouteTripsLengthsInMinutesToAndFromDestination(locationMap);
            System.out.println("Bus Trips Lengths in Minutes are: ");
            for (Map.Entry<String, List<Long>> tripListEntry: busTripMap.entrySet()) {
                System.out.println("{" + tripListEntry.getKey() + "=" + tripListEntry.getValue().toString()+ "}");
            }
            System.out.println();

        } else {
            System.out.println("Can't find this destination, please enter another destination");
        }
    }
}
