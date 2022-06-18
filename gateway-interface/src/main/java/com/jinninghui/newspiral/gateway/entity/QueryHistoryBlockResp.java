package com.jinninghui.newspiral.gateway.entity;

import com.jinninghui.newspiral.common.entity.block.Block;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
public class QueryHistoryBlockResp {
    @Getter @Setter
    List<Block> listBlock;
}
