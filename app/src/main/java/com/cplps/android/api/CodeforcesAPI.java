package com.cplps.android.api;

import com.cplps.android.api.models.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface CodeforcesAPI {

    // Get user information
    @GET("user.info")
    Call<CodeforcesResponse<List<CFUser>>> getUserInfo(@Query("handles") String handles);

    // Get user's rating change history
    @GET("user.rating")
    Call<CodeforcesResponse<List<CFRatingChange>>> getUserRating(@Query("handle") String handle);

    // Get user's submissions
    @GET("user.status")
    Call<CodeforcesResponse<List<CFSubmission>>> getUserSubmissions(
            @Query("handle") String handle,
            @Query("from") int from,
            @Query("count") int count);

    // Get list of contests
    @GET("contest.list")
    Call<CodeforcesResponse<List<CFContest>>> getContestList(@Query("gym") boolean gym);

    // Get problems from a contest
    @GET("contest.standings")
    Call<CodeforcesResponse<CFContestStandings>> getContestStandings(
            @Query("contestId") int contestId,
            @Query("from") int from,
            @Query("count") int count);
}
