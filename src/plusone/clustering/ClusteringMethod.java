package plusone.clustering;

import plusone.utils.PaperAbstract;

import java.util.List;

public interface ClusteringMethod {
    public void analysis(double trainPercent, double testWordPercent);
    //private void train(List<PaperAbstract> abstracts);
    //private void test(List<PaperAbstract> abstracts);
}