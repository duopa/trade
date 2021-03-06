package com.trade.controller;



import com.trade.job.SynDataJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    private SynDataJob synDataJob;

    @GetMapping(value = "/d/{date}")
    public String dailyOnlySym(@PathVariable String date) {
        synDataJob.dailyOnlySym(date, date);
        return "success";
    }

    @GetMapping(value = "/d/{startDate}/{endDate}")
    public String dailyOnlySym(@PathVariable String startDate,
                               @PathVariable String endDate) {
        synDataJob.dailyOnlySym(startDate, endDate);
        return "success";
    }

    @GetMapping(value = "/c")
    public String tradeCalSym() {
        synDataJob.tradeCalSym();
        return "success";
    }

    @GetMapping(value = "/b")
    public String stockBasicSym() {
        synDataJob.stockBasicSym();
        return "success";
    }

}

