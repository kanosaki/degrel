package degrel.visualize.view;

import degrel.visualize.viewmodel.GraphViewModel;
import degrel.visualize.viewmodel.graphdrawer.GraphDrawer;

import javax.swing.*;
import java.awt.*;

public class GraphView extends JPanel {
    /*
     * 描画の方針:
     * * 頂点・接続はGraphicsで直に描画
     * * 頂点はクリック，ドラッグ等の判定を持たせ，クリック等されればカスタマイズ可能なUIを表示する
     */
    private GraphViewModel viewmodel;
    private GraphDrawer drawer;

    public GraphView(GraphViewModel vm, GraphDrawer drawer) {
        if (vm == null) throw new NullPointerException("vm");
        if (drawer == null) throw new NullPointerException("drawer");
        this.viewmodel = vm;
        this.drawer = drawer;
    }

    @Override
    protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.drawLine(0, 0, 100, 100);
    }
}
