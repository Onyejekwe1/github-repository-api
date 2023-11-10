package com.redpharm.takehomechallenge.contract;

import java.io.Serializable;
import java.util.ArrayList;

public class GitResponseDTO implements Serializable {
    public int total_count;
    public boolean incomplete_results;
    public ArrayList<ItemDTO> items;
}

