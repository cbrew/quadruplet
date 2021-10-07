<template id="richnlg">


    <div class="card text-center m-3">
        <h5 class="card-header">{{name}}</h5>
        <input style="width:500px" v-model="msg" @keyup.enter="updateMsg">
        <button v-on:click="parse">RichNLG</button>
        <div class="card-body">{{msg}} </div>
    </div>
</template>

<script>
var p = Vue.component("richnlg", {
  name: "post-request",
  template: "#richnlg",
  data: () => ({
           msg:"",
           name: "Rich NLU"
        }),
  created() {
        fetch("/msg")
        .then(response => response.json())
        .then(data => this.msg = data.text);
  },

  methods: {
    parse: function(event) {
            alert(this.msg)
        },
    updateMsg: function() {
      const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ msg:this.msg,id:123 })
    };
    fetch("/msg", requestOptions)
      .then(response => response.json())
      .then(data => (this.msg = data.msg));

        }

  },

  });
</script>
