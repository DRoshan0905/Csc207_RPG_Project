package use_case.use_item;

public interface UseItemOutputBoundary {
    void prepareSuccessView(UseItemOutputData outputData);

    void prepareFailView(String error);
}
