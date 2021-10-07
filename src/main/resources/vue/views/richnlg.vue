<template id="richnlg">
    <div>
        <h5 class="card-header">{{name}}</h5>
        <input v-model="msg" @keyup.enter="updateMsg" style="width:300px">
        <button v-on:click="parse">RichNLU</button>
        <h5 class="card-header">Words</h5>
        <ul class="card-body">
           <li v-for="word in result.wordSpans" style="color:black; background-color:lightgray">
               {{word.start}}-{{word.label}}-{{word.end}}
           </li>
        </ul>
        <h5 class="card-header">Parts of speech</h5>
        <ul class="card-body">
            <li v-for="word in result.preTerminals" style="color:red; ; background-color:pink">
                {{word.start}}-{{word.label}}-{{word.end}}
            </li>
        </ul>
        <h5 class="card-header">Phrases</h5>
        <ul class="card-body">
            <li v-for="word in result.nonTerminals" style="color:blue;  background-color:lightblue">
                {{word.start}}-{{word.label}}-{{word.end}}
            </li>
        </ul>


    </div>
</template>

<script>
var p = Vue.component("richnlg", {
  name: "post-request",
  template: "#richnlg",
  data: () => ({
           msg:"",
           name: "Rich NLU",
           result: ""
        }),
  created() {
        fetch("/msg")
        .then(response => response.json())
        .then(data => this.msg = data.text);
  },

  methods: {
    parse: function(event) {
        const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ msg:this.msg,id:123 })
    };
      fetch("/parse", requestOptions)
      .then(response => response.json())
      .then(data => (this.result = data));

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
