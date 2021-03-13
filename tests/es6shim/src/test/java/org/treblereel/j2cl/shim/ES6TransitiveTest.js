import {ES6Test} from "./ES6Test.js";
import {ES6TestZZ} from "./ES6TestZZ.js";

class ES6TransitiveTest {

    constructor() {
        this.es6Test = new ES6Test();
        this.es6Test2 = new ES6TestZZ();
    }

    /**
    * @return {string}
    */
    getEs6Testid() {
        return this.es6Test.id;
    }

        /**
        * @return {string}
        */
        getEs6Test2id() {
            return this.es6Test2.id;
        }

}

export { ES6TransitiveTest };