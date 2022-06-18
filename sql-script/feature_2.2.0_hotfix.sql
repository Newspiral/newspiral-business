
use newspiral_cluster;
DROP INDEX STATE_KEY_INDEX ON state;
alter table state add index STATE_KEY_INDEX(CHANNEL_ID, STATE_KEY(200));

DROP INDEX STATE_KEY_INDEX ON state_attach;
alter table state_attach add index STATE_KEY_INDEX(CHANNEL_ID, STATE_KEY(200));
