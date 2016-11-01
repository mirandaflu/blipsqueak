/*
Copyright (C) 2015 Miranda Fluharty

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.blipsqueak.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;
import com.orm.query.Select;

import java.util.List;

public class BlipListActivity extends BaseActivity {
    private static final int detailReturnCode = 42;
    private static final int newblipReturnCode = 17;
    private List<Blip> items;
    private BlipArrayAdapter itemsAdapter;
    private DragSortListView lvItems;
    private Blip temporaryUndoItem;
    private int temporaryUndoPos;
    private boolean undoTapped = false;
    private int editPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bliplist);

        items = (Select.from(Blip.class).orderBy("rank ASC").list());
        lvItems = (DragSortListView) findViewById(R.id.lvItems);
        itemsAdapter = new BlipArrayAdapter();
        lvItems.setAdapter(itemsAdapter);

        setupListeners(this);

        DragSortController controller = new DragSortController(lvItems);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setSortEnabled(true);
        controller.setDragInitMode(1);
        controller.setRemoveEnabled(true);
        controller.setRemoveMode(DragSortController.FLING_REMOVE);
        lvItems.setFloatViewManager(controller);
        lvItems.setOnTouchListener(controller);
        lvItems.setDragEnabled(true);
        lvItems.setFloatAlpha((float) 0.8);

        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(lvItems);
        simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
        lvItems.setFloatViewManager(simpleFloatViewManager);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case newblipReturnCode: {
                if (resultCode == Activity.RESULT_OK) {
                    long blipID = (long) data.getExtras().getSerializable("blipID");
                    Blip blip = Blip.findById(Blip.class, blipID);
                    items.add(blip);
                    itemsAdapter.notifyDataSetChanged();
                }
                break;
            }
            case detailReturnCode: {
                if (resultCode == Activity.RESULT_OK) {
                    long blipID = (long) data.getExtras().getSerializable("blipID");
                    Blip blip = Blip.findById(Blip.class, blipID);
                    items.set(editPos, blip);
                    itemsAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (!undoTapped && temporaryUndoItem != null) {
            deleteTemporaryUndoItem();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bliplist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_help:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.help_bliplist_title))
                        .setMessage(getResources().getString(R.string.help_bliplist))
                        .setCancelable(true).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteTemporaryUndoItem() {
        String id = temporaryUndoItem.getId().toString();
        List<Squeak> squeaks = Select.from(Squeak.class).where("blip_id = " + id).list();
        for (Squeak s : squeaks) s.delete();
        List<Rule> rules = Select.from(Rule.class).where("blip_id = " + id).list();
        for (Rule r : rules) r.delete();
        temporaryUndoItem.delete();
    }

    private void setupListeners(final BlipListActivity blipListActivity) {
        lvItems.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter,
                                            View item, int pos, long id) {
                        editPos = pos;
                        Intent detailIntent = new Intent(blipListActivity, DetailActivity.class)
                                .putExtra("blipID", items.get(pos).getId());
                        startActivityForResult(detailIntent, detailReturnCode);
                    }
                }
        );
        lvItems.setDropListener(
                new DragSortListView.DropListener() {
                    @Override
                    public void drop(int from, int to) {
                        if (from != to) {
                            int increment = (to - from) / Math.abs(to - from);
                            for (int i = from + increment; i != to + increment; i += increment) {
                                items.get(i).rank = i - increment;
                                items.get(i).save();
                            }
                            Blip item = itemsAdapter.getItem(from);
                            item.rank = to;
                            item.save();
                            itemsAdapter.remove(item);
                            itemsAdapter.insert(item, to);
                            itemsAdapter.notifyDataSetChanged();
                        }
                    }
                }
        );
        lvItems.setRemoveListener(
                new DragSortListView.RemoveListener() {
                    @Override
                    public void remove(int which) {
                        temporaryUndoItem = itemsAdapter.getItem(which);
                        temporaryUndoPos = which;
                        undoTapped = false;
                        itemsAdapter.remove(itemsAdapter.getItem(which));
                        itemsAdapter.notifyDataSetChanged();

                        SnackbarManager.show(
                                Snackbar.with(getApplicationContext())
                                        .text(getResources().getString(R.string.item_deleted))
                                        .duration(3500)
                                        .actionLabel(getResources().getString(R.string.undo))
                                        .eventListener(new EventListener() {
                                            @Override
                                            public void onShow(Snackbar snackbar) {
                                            }

                                            @Override
                                            public void onShown(Snackbar snackbar) {
                                            }

                                            @Override
                                            public void onDismiss(Snackbar snackbar) {
                                                if (!undoTapped) deleteTemporaryUndoItem();
                                                undoTapped = false;
                                                temporaryUndoItem = null;
                                            }

                                            @Override
                                            public void onDismissed(Snackbar snackbar) {
                                            }
                                        })
                                        .actionListener(new ActionClickListener() {
                                            @Override
                                            public void onActionClicked(Snackbar snackbar) {
                                                undoTapped = true;
                                                itemsAdapter.insert(temporaryUndoItem, temporaryUndoPos);
                                                itemsAdapter.notifyDataSetChanged();
                                            }
                                        }), BlipListActivity.this);
                    }
                }
        );
    }

    public void addBlip(View target) {
        Intent newblipIntent = new Intent(this, NewblipActivity.class);
        startActivityForResult(newblipIntent, newblipReturnCode);
        overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
    }

    private class BlipArrayAdapter extends ArrayAdapter<Blip> {
        public BlipArrayAdapter() {
            super(BlipListActivity.this, R.layout.list_item_handle_left, R.id.text, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }
    }

}
