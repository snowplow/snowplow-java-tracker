package com.snowplowanalytics.snowplow.tracker;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class ContractManagerTest extends TestCase {


//    // TODO: move this into tests, see https://github.com/snowplow/snowplow-java-tracker/issues/5
//    public static void main(String[] args){
//        //Test cases - contracts enables or disables all contracts
//        boolean contracts = false;
//        PlowContractor<String> stringContractor = new PlowContractor<String>();
//        PlowContractor<Integer> integerContractor = new PlowContractor<Integer>();
//
//        //Able to make custom contracts like so
//        PlowContractor.Function<String> string_is_long = new PlowContractor.Function<String>() {
//            public boolean functionCheck(String input){
//                return !input.isEmpty() && input.length() > 10;
//            }
//            public String getErrorMsg(){
//                return "String Error - Input cannot be a string under length 10.";
//            }
//        };
//
//        //Check with checkCustomContract
//        stringContractor.customContract("dict", string_is_long);
//        assert stringContractor.checkCustomContract(contracts,"dict","Hello I Am Kevin");
//        assert integerContractor.checkContract(contracts, PlowContractor.positive_number, -10);
//        System.out.println((System.currentTimeMillis()/10));
//        Date date = new Date(System.currentTimeMillis());
//        System.out.println(date.toString());
//    }

    @Test
    public void testCheckContract() throws Exception {

    }

    @Test
    public void testCustomContract() throws Exception {

    }

    @Test
    public void testCheckCustomContract() throws Exception {

    }
}