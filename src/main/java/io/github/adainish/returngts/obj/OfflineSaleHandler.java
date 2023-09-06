package io.github.adainish.returngts.obj;

import io.github.adainish.returngts.ReturnGTS;

import java.util.ArrayList;
import java.util.List;

public class OfflineSaleHandler
{
    public List<OfflineSale> offlineSaleList = new ArrayList<>();

    public OfflineSaleHandler()
    {

    }

    public void add(GTSItem item)
    {
        this.offlineSaleList.add(new OfflineSale(item));
        ReturnGTS.log.warn("Added an offline sale to storage for uuid :" + item.seller);
    }

    public void handout()
    {

        if (!offlineSaleList.isEmpty())
        {
            this.offlineSaleList.forEach(OfflineSale::updateSeller);
            this.offlineSaleList.clear();
        }
    }
}
