package mock.brains.basecomponents.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import mock.brains.basecomponents.R;
import mock.brains.basecomponents.core.api.ApiManager;
import mock.brains.basecomponents.core.briteDb.Db;
import mock.brains.basecomponents.core.briteDb.DbManager;
import mock.brains.basecomponents.core.di.InjectHelper;
import mock.brains.basecomponents.core.model.User;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    @Inject
    DbManager dbManager;
    @Inject
    ApiManager apiManager;
    @Bind(R.id.showResult)
    Button vShowResultButton;
    @Bind(R.id.resultText)
    TextView vResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Observable<User> userObservable = apiManager.getUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Subscription getUsersSubscription = userObservable.subscribe(
                user -> {
                    if (!dbManager.isUserExist(user.getUserId())) {
                        dbManager.addUser(user);
                    }
                },
                e -> Timber.e(e, "GetUserError"),
                () -> {
                    Timber.i("GetUserCompleted");
                    vShowResultButton.setEnabled(true);
                }
        );

        addSubscription(getUsersSubscription);

        Subscription clickSubscription = RxView.clicks(vShowResultButton).subscribe(click -> {
            String resultText = "Users count in database = %1$d\n" +
                    "User with serverId = 1 existing in database = %2$b\n" +
                    "User with serverId = 1 toString\n%3$s";
            int usersCount = dbManager.getCountOfUsers();
            boolean isSomeUserExist = dbManager.isUserExist(1);
            String userToString = "";
            if (isSomeUserExist) {
                User someUser = dbManager.getUser(1, Db.COL_ID);
                userToString = someUser.toString();
            }
            vResultText.setText(String.format(resultText, usersCount, isSomeUserExist, userToString));
        });

        addSubscription(clickSubscription);

    }

    @Override
    protected void initDiComponent() {
        InjectHelper.getAppComponent(this).inject(this);
    }
}
