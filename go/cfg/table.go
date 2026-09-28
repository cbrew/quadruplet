package cfg

// itemTable maps a (symbol, span) key to an item, by open addressing: keys
// and items side by side in two arrays whose length is a power of two,
// probed linearly from a multiplicative hash, and doubled when half full. A
// key is never 0, since a span ends at 1 or later, so 0 marks an empty slot.
type itemTable struct {
	keys  []uint64
	items []int32
	n     int
	shift uint // 64 - log2(len(keys))
}

func newItemTable() itemTable {
	const bits = 10
	return itemTable{keys: make([]uint64, 1<<bits), items: make([]int32, 1<<bits), shift: 64 - bits}
}

func (t *itemTable) slot(k uint64) int { return int((k * 0x9E3779B97F4A7C15) >> t.shift) }

// get is the key's item, if it has one.
func (t *itemTable) get(k uint64) (int32, bool) {
	mask := len(t.keys) - 1
	for i := t.slot(k); ; i = (i + 1) & mask {
		switch t.keys[i] {
		case k:
			return t.items[i], true
		case 0:
			return 0, false
		}
	}
}

// add gives the key the item if it has none, and reports whether it did;
// either way it returns the key's item.
func (t *itemTable) add(k uint64, item int32) (int32, bool) {
	mask := len(t.keys) - 1
	i := t.slot(k)
	for ; t.keys[i] != 0; i = (i + 1) & mask {
		if t.keys[i] == k {
			return t.items[i], false
		}
	}
	t.keys[i], t.items[i] = k, item
	t.n++
	if 2*t.n >= len(t.keys) {
		t.grow()
	}
	return item, true
}

func (t *itemTable) grow() {
	keys, items := t.keys, t.items
	t.keys, t.items = make([]uint64, 2*len(keys)), make([]int32, 2*len(keys))
	t.shift--
	mask := len(t.keys) - 1
	for j, k := range keys {
		if k == 0 {
			continue
		}
		i := t.slot(k)
		for t.keys[i] != 0 {
			i = (i + 1) & mask
		}
		t.keys[i], t.items[i] = k, items[j]
	}
}
